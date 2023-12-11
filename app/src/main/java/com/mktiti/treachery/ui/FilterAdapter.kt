package com.mktiti.treachery.ui

import android.content.Context
import android.content.res.Configuration
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.mktiti.treachery.R
import com.mktiti.treachery.core.Card
import com.mktiti.treachery.core.CardType
import com.mktiti.treachery.manager.CardManager
import com.mktiti.treachery.manager.IconManager

class FilterAdapter(
    private val context: AddCardActivity,
    private val iconManager: IconManager,
    private val cardClickCallback: (Card, Int) -> Unit
    ) : RecyclerView.Adapter<FilterAdapter.CardHolder>() {

    class CardHolder(view: View) : RecyclerView.ViewHolder(view) {
        val icon: ImageView = view.findViewById(R.id.type_icon)
        val name: TextView = view.findViewById(R.id.card_name)
        val type: TextView = view.findViewById(R.id.type_name)
    }

    private var currentFilters = mutableListOf<String>()
    private var cards = CardManager.getCards(context).toList()

    val allFilterButton = context.findViewById<Button>(R.id.allFilter).apply {
        setOnClickListener {
            removeAllFilters()
        }
    }

    val weaponFilterButton = context.findViewById<Button>(R.id.weaponFilter).apply {
        setOnClickListener {
            filterBy("weapon", R.id.weaponFilter)
        }
    }
    val defenseFilterButton = context.findViewById<Button>(R.id.defenseFilter).apply {
        setOnClickListener {
            filterBy("defense", R.id.defenseFilter)
        }
    }

    val projectileFilterButton = context.findViewById<Button>(R.id.projectileFilter).apply {
        setOnClickListener {
            filterBy("projectile", R.id.projectileFilter)
        }
    }

    val poisonFilterButton = context.findViewById<Button>(R.id.poisonFilter).apply {
        setOnClickListener {
            filterBy("poison", R.id.poisonFilter)
        }
    }

    val specialFilterButton = context.findViewById<Button>(R.id.specialFilter).apply {
        setOnClickListener {
            filterBy("special", R.id.specialFilter)
        }
    }

    val karamsFilterButton = context.findViewById<Button>(R.id.karamaFilter).apply {
        setOnClickListener {
            filterBy("karama", R.id.karamaFilter)
        }
    }

    val leaderFilterButton = context.findViewById<Button>(R.id.leaderFilter).apply {
        setOnClickListener {
            filterBy("leader", R.id.leaderFilter)
        }
    }

    val richeseFilterButton = context.findViewById<Button>(R.id.richeseFilter).apply {
        setOnClickListener {
            filterBy("richese", R.id.richeseFilter)
        }
    }


    val worthlessFilterButton = context.findViewById<Button>(R.id.worthlessFilter).apply {
        setOnClickListener {
            filterBy("worthless", R.id.worthlessFilter)
        }
    }

    val spiceFilterButton = context.findViewById<Button>(R.id.spiceFilter).apply {
        setOnClickListener {
            filterBy("spice", R.id.spiceFilter)
        }
    }

    val movementFilterButton = context.findViewById<Button>(R.id.movementFilter).apply {
        setOnClickListener {
            filterBy("movement", R.id.movementFilter)
        }
    }



    override fun getItemCount() = cards.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FilterAdapter.CardHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.card_item, parent, false)
        return FilterAdapter.CardHolder(itemView)
    }

    private fun isDark(): Boolean = when (context.resources?.configuration?.uiMode?.and(
        Configuration.UI_MODE_NIGHT_MASK)) {
        Configuration.UI_MODE_NIGHT_YES -> true
        Configuration.UI_MODE_NIGHT_NO -> false
        Configuration.UI_MODE_NIGHT_UNDEFINED -> false
        else -> false
    }

    override fun onBindViewHolder(holder: FilterAdapter.CardHolder, position: Int) {
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

    private fun buttonFilterUnset(buttonViewId: Int) {
        context.findViewById<Button>(buttonViewId).alpha = 1f
    }

    private fun buttonFilterSet(buttonViewId: Int) {
        context.findViewById<Button>(buttonViewId).alpha = 0.6f
    }

    private fun filterBy(keyword: String, buttonViewId: Int) {
        if(currentFilters.contains(keyword)) {
            buttonFilterUnset(buttonViewId)
            currentFilters.remove(keyword)
        } else {
            buttonFilterSet(buttonViewId)
            currentFilters.add((keyword))
        }
        cards = filterCards(currentFilters)
        notifyDataSetChanged()
    }

    fun resetFilters(){
        cards = CardManager.getCards(context).toList()
        currentFilters.clear()
        notifyDataSetChanged()
    }

    private fun removeAllFilters() {
        resetFilters()
        buttonFilterUnset(R.id.weaponFilter)
        buttonFilterUnset(R.id.defenseFilter)
        buttonFilterUnset(R.id.projectileFilter)
        buttonFilterUnset(R.id.poisonFilter)
        buttonFilterUnset(R.id.specialFilter)
        buttonFilterUnset(R.id.karamaFilter)
        buttonFilterUnset(R.id.leaderFilter)
        buttonFilterUnset(R.id.richeseFilter)
        buttonFilterUnset(R.id.worthlessFilter)
        buttonFilterUnset(R.id.spiceFilter)
        buttonFilterUnset(R.id.movementFilter)
    }

    public fun filterCards(keywords: MutableList<String>): List<Card> {
        var cards = CardManager.getCards(context).toList();

        for(keyword in keywords) {
            cards = cards.filter {
                if (it != null) {
                    it.tags.contains(keyword, ignoreCase = true) || it.type.name.contains(
                        keyword,
                        ignoreCase = true
                    )
                } else {
                    false
                }
            }.toMutableList()
        }
        return cards
    }
}
