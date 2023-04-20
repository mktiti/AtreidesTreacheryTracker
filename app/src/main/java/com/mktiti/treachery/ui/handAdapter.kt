package com.mktiti.treachery.ui

import android.content.Context
import android.content.res.Configuration
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.mktiti.treachery.core.Card
import com.mktiti.treachery.core.CardType
import com.mktiti.treachery.manager.IconManager
import com.mktiti.treachery.R

class HandAdapter(
    private val context: Context,
    private val iconManager: IconManager,
    initCards: List<Card?>,
    private val deleteCallback: (() -> Unit) -> Unit,
    private val cardClickCallback: (Card, Int) -> Unit,
) : RecyclerView.Adapter<HandAdapter.CardHolder>() {

    class CardHolder(view: View) : RecyclerView.ViewHolder(view) {
        val icon: ImageView = view.findViewById(R.id.type_icon)
        val name: TextView = view.findViewById(R.id.card_name)
        val type: TextView = view.findViewById(R.id.type_name)
    }

    private val cards = initCards.toMutableList()

    val stored: List<Card?>
        get() = cards

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CardHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.card_item, parent, false)
        return CardHolder(itemView)
    }

    override fun getItemCount() = cards.size

    private fun isDark(): Boolean = when (context.resources?.configuration?.uiMode?.and(Configuration.UI_MODE_NIGHT_MASK)) {
        Configuration.UI_MODE_NIGHT_YES -> true
        Configuration.UI_MODE_NIGHT_NO -> false
        Configuration.UI_MODE_NIGHT_UNDEFINED -> false
        else -> false
    }

    override fun onBindViewHolder(holder: CardHolder, position: Int) {
        val isDark = isDark()
        val colorMapper = if (isDark) CardType::darkColor else CardType::lightColor
        val defColor = if (isDark) R.color.textColorDark else R.color.textColorLight

        val card = cards[position]
        if (card == null) {
            holder.name.text = context.getString(R.string.unknown)
            holder.name.setTextColor(context.getColor(defColor))
            holder.type.text = context.getString(R.string.unknown_type)
            holder.icon.setImageDrawable(iconManager.unknownCard())
        } else {
            with(card) {
                holder.name.text = name
                holder.name.setTextColor(colorMapper(card.type))
                holder.icon.setImageDrawable(iconManager[type])
                holder.type.text = card.tags
            }
        }

        holder.itemView.setOnClickListener {
            cards[position]?.let { card -> cardClickCallback(card, position) }

        }
    }

    operator fun plusAssign(card: Card?) {
        cards += card
        notifyItemInserted(cards.size - 1)
    }

    operator fun minusAssign(viewHolder: RecyclerView.ViewHolder) {
        val pos = viewHolder.adapterPosition
        val removed = cards.removeAt(pos)
        notifyItemRemoved(pos)
        deleteCallback {
            cards.add(pos, removed)
            notifyItemInserted(pos)
        }
    }

    fun cardTransferred(viewHolder: RecyclerView.ViewHolder) {
        val pos = viewHolder.adapterPosition
        val removed = cards.removeAt(pos)
        notifyItemRemoved(pos)
    }

}
