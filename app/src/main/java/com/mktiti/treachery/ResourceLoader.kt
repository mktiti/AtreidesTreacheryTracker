package com.mktiti.treachery

import android.content.Context
import android.graphics.drawable.Drawable
import java.util.concurrent.locks.ReentrantLock

object ResourceLoader {

    private val lock = ReentrantLock()
    private var singleton: IconManager? = null

    private fun loadImage(context: Context, file: String): Drawable {
        return Drawable.createFromStream(context.assets.open(file), null)
    }

    private inline fun <reified E : Enum<E>> loadAssetIcons(context: Context, folder: String, mapper: (E) -> String): Map<E, Drawable> {
        return enumValues<E>().map { value ->
            value to loadImage(context, "icons/$folder/${mapper(value)}.png")
        }.toMap()
    }

    fun getIconManager(context: Context): IconManager {
        val stored = singleton

        if (stored == null) {
            synchronized(lock) {
                return singleton ?: loadIconManager(context).apply {
                    singleton = this
                }
            }
        } else {
            return stored
        }
    }

    private fun loadIconManager(context: Context): IconManager {
        val playerIcons = loadAssetIcons(context, "factions", Player::id)
        val cardTypeIcons = loadAssetIcons(context, "cards", CardType::id)

        return MapIconManager(
            unknownCard = loadImage(context, "icons/cards/unknown.png"),
            noCard = loadImage(context, "icons/cards/no_card.png"),
            cardTypeIcons = cardTypeIcons,
            playerIcons = playerIcons
        )
    }

}
