package com.mktiti.treachery.ui

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Debug
import android.util.Log
import android.widget.TextView
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.mktiti.treachery.*
import com.mktiti.treachery.core.*
import com.mktiti.treachery.manager.ResourceLoader
import kotlinx.android.synthetic.main.activity_card_details.*
import java.util.Collections.copy
import java.util.concurrent.locks.ReentrantLock

class CardDetailsActivity : AppCompatActivity() {

    private lateinit var card: Card
    private var cardPosition: Int = 0
    private lateinit var owner: Player
    private var newOwner: Player? = null
    private lateinit var tags: TextView
    private lateinit var description: TextView
    private lateinit var specialUsage: TextView


    private lateinit var changeOwnerButton: FloatingActionButton
    private val changeOwnerLock = ReentrantLock()

    private var allHands: HandState = HandState(listOf<PlayerHand>())

    companion object {
        const val CARD_DATA_KEY = "card_data"
    }

    private val stateString: String
        get() = CardState(
            card, cardPosition, owner, newOwner
        ).json()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_card_details)
        setSupportActionBar(findViewById(R.id.toolbar))

        val cardStateJson = savedInstanceState?.getString(CardDetailsActivity.CARD_DATA_KEY)
            ?: intent.extras?.getString(CardDetailsActivity.CARD_DATA_KEY)
            ?: throw IllegalArgumentException("Card missing!")

        val cardState = CardState.parse(cardStateJson)
        card = cardState.card
        cardPosition = cardState.cardPosition
        owner = cardState.owner

        title = card.name

        tags = findViewById<TextView>(R.id.card_tags).apply {
            text = card.tags
        }
        description = findViewById<TextView>(R.id.card_description).apply {
            text = card.description
        }
        card.choam?.let {
            specialUsage = findViewById<TextView>(R.id.special_usage).apply {
                text = "Choam only:\n" + card.choam
            }
        }

        changeOwnerButton = findViewById<FloatingActionButton>(R.id.change_owner).apply {
            setOnClickListener {
                val available: List<Player> =
                    allHands.hands
                        .filter { playerHand -> playerHand.player != owner &&
                            playerHand.cards.size < playerHand.player.maxCards }
                        .map { playerHand -> playerHand.player }
                SelectUtil.promptHouse(
                    this@CardDetailsActivity,
                    this@CardDetailsActivity.getString(R.string.change_owner_title),
                    available
                ) { player ->
                    changeCardOwner(player)
                }
            }
        }

        val allHandStateJson = savedInstanceState?.getString(PlayerHandActivity.APPLICABLE_PLAYERS_FOR_CARD_TRANSFER) ?:
        intent.extras?.getString(PlayerHandActivity.APPLICABLE_PLAYERS_FOR_CARD_TRANSFER) ?:
        throw IllegalArgumentException("Player hand missing!")

        allHands = HandState.parse(allHandStateJson)
    }

    private fun changeCardOwner(nextOwner: Player) {
        Snackbar.make(findViewById(R.id.card_coord),
            R.string.card_changed_owner, Snackbar.LENGTH_LONG).apply {
            setAction(R.string.change_owner_undo) {
                guardedChangeOwnerAction {  newOwner = null }
            }
            show()
        }
        guardedChangeOwnerAction { this.newOwner = nextOwner }
    }

    private fun guardedChangeOwnerAction(action: () -> Unit) {
        synchronized(changeOwnerLock) {
            action()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        outState.putString(CardDetailsActivity.CARD_DATA_KEY, stateString)
    }

    override fun onBackPressed() {
        Intent().apply {
            putExtra(CardDetailsActivity.CARD_DATA_KEY, stateString)
            setResult(Activity.RESULT_OK, this)
        }

        finish()
    }
}