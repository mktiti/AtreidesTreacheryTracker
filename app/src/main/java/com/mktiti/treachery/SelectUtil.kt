package com.mktiti.treachery

import android.app.AlertDialog
import android.content.Context
import android.graphics.drawable.Drawable
import android.widget.ArrayAdapter

object SelectUtil {

    fun <T> promptSelect(context: Context, title: String, available: List<T>, formatter: T.() -> String, callback: (T) -> Unit) {
        val adapter = ArrayAdapter<String>(context, android.R.layout.select_dialog_item).apply {
            available.forEach { add(it.formatter()) }
        }

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

    fun <T> prompt(context: Context, title: String, available: List<T>, icon: T.() -> Drawable, formatter: T.() -> String, callback: (T) -> Unit) {
        val adapter = IconAdapter(available, icon, formatter)

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

    fun promptHouse(context: Context, available: List<Player>, callback: (Player) -> Unit) {
        val iconManager = ResourceLoader.getIconManager(context)

        prompt(
            context,
            context.getString(R.string.add_player_title),
            available,
            iconManager::get,
            Player::niceName,
            callback
        )
    }

    fun promptCard(context: Context, callback: (Card) -> Unit) {
        val iconManager = ResourceLoader.getIconManager(context)

        prompt(
            context,
            context.getString(R.string.select_type),
            CardManager.getCards(context).toList(),
            { iconManager[type] },
            Card::name,
            callback
        )
    }

    fun promptCardType(context: Context, callback: (CardType) -> Unit) {
        val iconManager = ResourceLoader.getIconManager(context)
        val available = CardType.values().toList()
        val adapter = IconAdapter(available, iconManager::get, CardType::niceName)

        AlertDialog.Builder(context).apply {
            setTitle(context.getString(R.string.select_type))
            setNegativeButton(android.R.string.cancel) { dialog, _ ->
                dialog.dismiss()
            }

            setAdapter(adapter) { dialog, pos ->
                dialog.dismiss()
                callback(available[pos])
            }
        }.show()
    }

}
