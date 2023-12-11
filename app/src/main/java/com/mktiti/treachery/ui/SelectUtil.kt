package com.mktiti.treachery.ui

import android.app.AlertDialog
import android.content.Context
import android.graphics.drawable.Drawable
import com.mktiti.treachery.manager.CardManager
import com.mktiti.treachery.R
import com.mktiti.treachery.manager.ResourceLoader
import com.mktiti.treachery.core.Card
import com.mktiti.treachery.core.Player

object SelectUtil {

    private fun <T> prompt(context: Context, title: String, available: List<T>, icon: T.() -> Drawable, formatter: T.() -> String, callback: (T) -> Unit) {
        val adapter =
            IconAdapter(available, icon, formatter)

        AlertDialog.Builder(context).apply {
            setTitle(title)
            setNegativeButton(android.R.string.cancel) { dialog, _ ->
                dialog.dismiss()
            }

            setAdapter(adapter) { dialog, pos ->
                dialog.dismiss()
                callback(available[pos])
            }
        }.show()
    }

    fun promptHouse(context: Context, title: String, available: List<Player>, callback: (Player) -> Unit) {
        val iconManager =
            ResourceLoader.getIconManager(context)

        prompt(
            context,
            title,
            available,
            iconManager::get,
            Player::niceName,
            callback
        )
    }

    fun promptCard(context: Context, callback: (Card) -> Unit) {
        val iconManager =
            ResourceLoader.getIconManager(context)

        prompt(
            context,
            context.getString(R.string.select_type),
            CardManager.getCards(context).toList(),
            { iconManager[type] },
            Card::name,
            callback
        )
    }

}
