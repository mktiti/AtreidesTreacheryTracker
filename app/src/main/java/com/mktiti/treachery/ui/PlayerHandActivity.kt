package com.mktiti.treachery.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.get
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.mktiti.treachery.*
import com.mktiti.treachery.core.*
import com.mktiti.treachery.manager.ResourceLoader
import java.util.concurrent.locks.ReentrantLock

class PlayerHandActivity : AppCompatActivity() {

    companion object {
        const val SHOW_CARD_DETAILS = 1
        const val HAND_DATA_KEY = "hand_data"
        const val APPLICABLE_PLAYERS_FOR_CARD_TRANSFER = "applicable_players_for_card_transfer"
    }

    private val addLock = ReentrantLock()

    private lateinit var player: Player
    private lateinit var handAdapter: HandAdapter
    private lateinit var cardList: RecyclerView
    private lateinit var cardTransfers: MutableList<CardTransfer>

    private lateinit var notes: EditText
    private lateinit var cardAdd: FloatingActionButton
    private lateinit var unknownAdd: FloatingActionButton

    private var allHands: HandState = HandState(listOf<PlayerHand>())

    private val canAddMore: Boolean
        get() = handAdapter.stored.size < player.maxCards

    private val stateString: String
        get() = PlayerHand(
            player, handAdapter.stored, notes.text.toString(), cardTransfers
        ).json()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_player_hand)
        setSupportActionBar(findViewById(R.id.toolbar))

        val handStateJson = savedInstanceState?.getString(HAND_DATA_KEY) ?:
            intent.extras?.getString(HAND_DATA_KEY) ?:
            throw IllegalArgumentException("Player hand missing!")

        val hand = PlayerHand.parse(handStateJson)
        player = hand.player
        title = player.niceName

        cardTransfers = mutableListOf<CardTransfer>()

        handAdapter = HandAdapter(
            this,
            ResourceLoader.getIconManager(this),
            hand.cards,
            this::onCardDelete,
            this::showCardRequest
        )
        cardList = findViewById<RecyclerView>(R.id.card_list).apply {
            layoutManager = LinearLayoutManager(context)
            adapter = handAdapter
        }
        val callback = SwipeDeleteCallback {
            handAdapter -= it
        }
        ItemTouchHelper(callback).attachToRecyclerView(cardList)

        cardAdd = findViewById<FloatingActionButton>(R.id.card_add).apply {
            setOnClickListener {
                SelectUtil.promptCard(this@PlayerHandActivity) { card ->
                    addCard(card)
                }
            }
        }

        unknownAdd = findViewById<FloatingActionButton>(R.id.unknown_card_add).apply {
            setOnClickListener {
                addCard(null)
            }
        }

        notes = findViewById<EditText>(R.id.notes).apply {
            setText(hand.note)
        }

        val allHandStateJson = savedInstanceState?.getString(APPLICABLE_PLAYERS_FOR_CARD_TRANSFER) ?:
        intent.extras?.getString(APPLICABLE_PLAYERS_FOR_CARD_TRANSFER) ?:
        throw IllegalArgumentException("Player hand missing!")

        allHands = HandState.parse(allHandStateJson)

        onCardUpdate()
    }

    private fun guardedAddAction(action: () -> Unit) {
        if (canAddMore) {
            synchronized(addLock) {
                if (canAddMore) {
                    action()
                    onCardUpdate()
                }
            }
        }
    }

    private fun addCard(card: Card?) {
        guardedAddAction {
            handAdapter += card
        }
    }

    private fun showCardRequest(card: Card, position: Int) {
        val cardState = CardState(card, position, player)
        val intent = Intent(this, CardDetailsActivity::class.java)
        intent.putExtra(CardDetailsActivity.CARD_DATA_KEY, cardState.json())
        intent.putExtra(PlayerHandActivity.APPLICABLE_PLAYERS_FOR_CARD_TRANSFER, PlayerHand.gson.toJson(allHands))
        startActivityForResult(intent, PlayerHandActivity.SHOW_CARD_DETAILS)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PlayerHandActivity.SHOW_CARD_DETAILS) {
            if (resultCode == Activity.RESULT_OK) {
                val jsonData = data?.getStringExtra(CardDetailsActivity.CARD_DATA_KEY) ?: return
                val cardState = CardState.parse(jsonData)
                if (cardState.newOwner != null) {
                    cardList.findViewHolderForAdapterPosition(cardState.cardPosition)?.let {
                        Log.v("card position", cardState.cardPosition.toString())
                        handAdapter.cardTransferred(it)
                        cardTransfers.add(CardTransfer(cardState.owner, cardState.newOwner, cardState.card))
                    }
                }
            }
        }
    }

    private fun onCardUpdate() {
        if (canAddMore) {
            cardAdd.show()
            unknownAdd.show()
        } else {
            cardAdd.hide()
            unknownAdd.hide()
        }
    }

    private fun onCardDelete(undo: () -> Unit) {
        onCardUpdate()

        Snackbar.make(findViewById(R.id.hand_coord),
            R.string.card_removed, Snackbar.LENGTH_LONG).apply {
            setAction(R.string.player_remove_undo) {
                guardedAddAction(undo)
            }
            show()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        outState.putString(HAND_DATA_KEY, stateString)
    }

    override fun onBackPressed() {
        Intent().apply {
            putExtra(HAND_DATA_KEY, stateString)
            setResult(Activity.RESULT_OK, this)
        }

        finish()
    }
}