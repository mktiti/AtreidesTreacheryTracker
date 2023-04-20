package com.mktiti.treachery.manager

import android.content.Context
import com.google.gson.Gson
import com.mktiti.treachery.core.Card
import com.mktiti.treachery.core.CardType
import java.io.InputStreamReader
import java.util.concurrent.locks.ReentrantLock

object CardManager {

    private data class CardData(
        val name: String,
        val type: String,
        val tags: String,
        val description: String
    ) {

        fun toCard() = Card(
            name,
            CardType.fromId(type),
            tags,
            description
        )

    }

    private val gson = Gson()

    private val lock = ReentrantLock()
    private var cards: List<Card>? = null

    fun getCards(context: Context): List<Card> {
        val stored = cards

        if (stored == null) {
            synchronized(lock) {
                return cards
                    ?: loadCards(context)
                        .apply {
                    cards = this
                }
            }
        } else {
            return stored
        }
    }

    private fun loadCards(context: Context): List<Card> {
        return context.assets.open("cards.json").use { stream ->
            InputStreamReader(stream).use { reader ->
                gson.fromJson(reader, Array<CardData>::class.java).map { it.toCard() }
            }
        }
    }

}
