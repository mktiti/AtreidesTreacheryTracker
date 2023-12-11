package com.mktiti.treachery.ui

import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.mktiti.treachery.manager.IconManager
import com.mktiti.treachery.core.Player
import com.mktiti.treachery.core.PlayerHand
import com.mktiti.treachery.R
import com.mktiti.treachery.core.Card

class PlayerAdapter(
    private val iconManager: IconManager,
    initPlayers: List<PlayerHand>,
    private val clickCallback: (PlayerHand) -> Unit,
    private val deleteCallback: (Player, () -> Unit) -> Unit
) : RecyclerView.Adapter<PlayerAdapter.PlayerHolder>() {

    class PlayerHolder(view: View) : RecyclerView.ViewHolder(view) {
        val name: TextView = view.findViewById(R.id.faction_name)
        val icon: ImageView = view.findViewById(R.id.faction_icon)
        val cardsLayout: LinearLayout = view.findViewById(R.id.cards_layout)
    }

    private val players = if(initPlayers.isNotEmpty())
                                initPlayers.toMutableList()
                            else
                                mutableListOf<PlayerHand>(
                                    PlayerHand(
                                        Player.BIDDING,
                                        listOf<Card?>(),
                                        ""
                                    ),
                                    PlayerHand(Player.DISCARD_PILE, listOf<Card?>(), "")
                                );

    val stored: List<PlayerHand>
        get() = players

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlayerHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.player_item, parent, false)
        return PlayerHolder(itemView)
    }

    override fun getItemCount() = players.size

    override fun onBindViewHolder(holder: PlayerHolder, position: Int) {
        with(players[position]) {
            holder.name.text = player.niceName
            holder.icon.setImageDrawable(iconManager[player])
            holder.cardsLayout.removeAllViews()

            holder.itemView.setOnClickListener {
                clickCallback(this)
            }

            val iconParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 0, 8, 0)
            }

            fun addIcon(icon: Drawable) {
                val cardIcon = ImageView(holder.cardsLayout.context)
                cardIcon.setImageDrawable(icon)
                cardIcon.maxWidth = 16
                cardIcon.maxHeight = 16
                cardIcon.layoutParams = iconParams

                holder.cardsLayout.addView(cardIcon)
            }

            var shownCards = 0
            cards.forEach { card ->
                shownCards += 1
                if(shownCards > 16)  {
                    return@forEach
                }
                addIcon(if (card == null) iconManager.unknownCard() else iconManager[card.type])
            }
            repeat(player.maxCards - cards.size) {
                shownCards += 1
                if(shownCards > 16)  {
                    return@repeat
                }
                addIcon(iconManager.noCard())
            }
        }
    }

    operator fun plusAssign(hand: PlayerHand) {
        players += hand
        notifyItemInserted(players.size - 1)
    }

    operator fun minusAssign(viewHolder: RecyclerView.ViewHolder) {
        val pos = viewHolder.adapterPosition
        val removed = players.removeAt(pos)
        notifyItemRemoved(pos)
        deleteCallback(removed.player) {
            players.add(pos, removed)
            notifyItemInserted(pos)
        }
    }

    fun update(hand: PlayerHand) {
        val index = players.withIndex().find { it.value.player == hand.player }?.index
        if (index == null) {
            this += hand
        } else {
            players[index] = hand
            notifyItemChanged(index)
        }
    }

    fun changeOwner(card: Card, previousOwner: Player, newOwner: Player) {
        val previousOwnerIndex = players.withIndex().find { it.value.player == previousOwner }?.index
        val newOwnerIndex = players.withIndex().find { it.value.player == newOwner }?.index
        if (previousOwnerIndex != null && newOwnerIndex != null) {
            val previousOwnerHand = PlayerHand(players[previousOwnerIndex].player, players[previousOwnerIndex].cards - card, players[previousOwnerIndex].note)
            players[previousOwnerIndex] = previousOwnerHand

            val newOwnerHand = PlayerHand(players[newOwnerIndex].player, players[newOwnerIndex].cards + card, players[newOwnerIndex].note)
            players[newOwnerIndex] = newOwnerHand

            notifyItemChanged(previousOwnerIndex)
            notifyItemChanged(newOwnerIndex)
        }
    }
}

class SwipeDeleteCallback(
    private val onRemove: (RecyclerView.ViewHolder) -> Unit
) : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean = false

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        onRemove(viewHolder)
    }

}
