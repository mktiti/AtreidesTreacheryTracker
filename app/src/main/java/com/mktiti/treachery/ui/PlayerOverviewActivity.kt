package com.mktiti.treachery.ui

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.mktiti.treachery.*
import com.mktiti.treachery.core.HandState
import com.mktiti.treachery.core.Player
import com.mktiti.treachery.core.PlayerHand
import com.mktiti.treachery.manager.ResourceLoader
import java.util.concurrent.locks.ReentrantLock


class PlayerOverviewActivity : AppCompatActivity() {

    private companion object {
        const val START_HAND = 1
        const val DATA_KEY = "hands_data"
    }

    private val addLock = ReentrantLock()

    private lateinit var playerAdapter: PlayerAdapter
    private lateinit var playerList: RecyclerView

    private lateinit var playerAdd: FloatingActionButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_player_overview)
        setSupportActionBar(findViewById(R.id.toolbar))

        val state = (
                savedInstanceState?.getString(DATA_KEY) ?:
                intent.extras?.getString(DATA_KEY) ?:
                getPreferences(Context.MODE_PRIVATE)?.getString(DATA_KEY, null)
            )?.let { HandState.parse(it).hands }
                ?: Player.values().map { it.startHand() }

        playerAdapter = PlayerAdapter(
            ResourceLoader.getIconManager(this),
            state,
            this::playerClick,
            this::onPlayerDelete
        )
        playerList = findViewById<RecyclerView>(R.id.players_list).apply {
            layoutManager = LinearLayoutManager(context)
            adapter = playerAdapter
        }
        val callback = SwipeDeleteCallback {
            playerAdapter -= it
        }
        ItemTouchHelper(callback).attachToRecyclerView(playerList)

        playerAdd = findViewById<FloatingActionButton>(R.id.player_add).apply {
            setOnClickListener {
                val available: List<Player> = Player.values().toList() - playerAdapter.stored.map { it.player }
                SelectUtil.promptHouse(
                    this@PlayerOverviewActivity,
                    this@PlayerOverviewActivity.getString(R.string.add_player_title),
                    available
                ) { player ->
                    addPlayer(player.startHand())
                }
            }
        }

        onPlayerUpdate()
    }

    private fun canAdd(player: Player?) =
        playerAdapter.stored.size < Player.values().size &&
        (player == null || playerAdapter.stored.find { it.player == player} == null)

    private fun guardedAddAction(player: Player, action: () -> Unit) {
        if (canAdd(player)) {
            synchronized(addLock) {
                if (canAdd(player)) {
                    action()
                    onPlayerUpdate()
                }
            }
        }
    }

    private fun addPlayer(hand: PlayerHand) {
        guardedAddAction(hand.player) {
            playerAdapter += hand
        }
    }

    private fun onPlayerUpdate() {
        if (canAdd(null)) {
            playerAdd.show()
        } else {
            playerAdd.hide()
        }
    }

    private fun playerClick(playerHand: PlayerHand) {
        val intent = Intent(this, PlayerHandActivity::class.java)
        intent.putExtra(PlayerHandActivity.HAND_DATA_KEY, playerHand.json())
        intent.putExtra(PlayerHandActivity.APPLICABLE_PLAYERS_FOR_CARD_TRANSFER, HandState(playerAdapter.stored).json())
        startActivityForResult(intent, START_HAND)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == START_HAND) {
            if (resultCode == Activity.RESULT_OK) {
                val jsonData = data?.getStringExtra(PlayerHandActivity.HAND_DATA_KEY) ?: return
                val hand = PlayerHand.parse(jsonData)
                playerAdapter.update(hand)

                hand.cardTransfers.forEach { cardTransfer ->
                    playerAdapter.changeOwner(cardTransfer.card, cardTransfer.previousOwner, cardTransfer.newOwner)
                }
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        outState.putString(
            DATA_KEY, HandState(
                playerAdapter.stored
            ).json())
    }

    override fun onStop() {
        getPreferences(Context.MODE_PRIVATE)?.edit()?.apply {
            putString(
                DATA_KEY, HandState(
                    playerAdapter.stored
                ).json())
            apply()
        }

        super.onStop()
    }

    private fun onPlayerDelete(player: Player, undo: () -> Unit) {
        onPlayerUpdate()

        Snackbar.make(findViewById(R.id.player_coord),
            R.string.player_removed, Snackbar.LENGTH_LONG).apply {
            setAction(R.string.player_remove_undo) {
                guardedAddAction(player, undo)
            }
            show()
        }
    }

}