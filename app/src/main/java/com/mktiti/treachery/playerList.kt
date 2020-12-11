package com.mktiti.treachery

import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView

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

    private val players = initPlayers.toMutableList()

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

            cards.forEach { card ->
                addIcon(if (card == null) iconManager.unknownCard() else iconManager[card.type])
            }

            repeat(player.maxCards - cards.size) {
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
