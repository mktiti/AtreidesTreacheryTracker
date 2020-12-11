package com.mktiti.treachery.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.mktiti.treachery.*
import com.mktiti.treachery.core.Card
import com.mktiti.treachery.core.Player
import com.mktiti.treachery.core.PlayerHand
import com.mktiti.treachery.manager.ResourceLoader
import java.util.concurrent.locks.ReentrantLock

class PlayerHandActivity : AppCompatActivity() {

    companion object {
        const val HAND_DATA_KEY = "hand_data"
    }

    private val addLock = ReentrantLock()

    private lateinit var player: Player
    private lateinit var handAdapter: HandAdapter
    private lateinit var cardList: RecyclerView

    private lateinit var notes: EditText
    private lateinit var cardAdd: FloatingActionButton
    private lateinit var unknownAdd: FloatingActionButton

    private val canAddMore: Boolean
        get() = handAdapter.stored.size < player.maxCards

    private val stateString: String
        get() = PlayerHand(
            player, handAdapter.stored, notes.text.toString()
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

        handAdapter = HandAdapter(
            this,
            ResourceLoader.getIconManager(this),
            hand.cards,
            this::onCardDelete,
            this::changeCardRequest
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

    private fun changeCardRequest(onUpdate: (Card?) -> Unit) {
        SelectUtil.promptCard(this@PlayerHandActivity) { card ->
            onUpdate(card)
            onCardUpdate()
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