package com.mktiti.treachery

import android.annotation.SuppressLint
import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.Spinner
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment

class CardSelectDialog(
    private val players: List<Player>?
) : DialogFragment() {

    private lateinit var typeSpinner: Spinner
    private lateinit var cardSpinner: Spinner
    private lateinit var houseSpinner: Spinner

    @SuppressLint("InflateParams")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val activity = activity ?: throw IllegalStateException("Activity cannot be null")

        val inflater = activity.layoutInflater
        val layout = inflater.inflate(R.layout.card_select_area, null)

        val iconManager = ResourceLoader.getIconManager(activity)
        val types = CardType.values().toList()
        val cards = CardManager.getCards(activity)

        return AlertDialog.Builder(activity).apply {
            setView(layout)

            typeSpinner = layout.findViewById<Spinner>(R.id.type_spinner).apply {
                adapter = IconAdapter(types, iconManager::get, CardType::niceName)
                onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(
                        parent: AdapterView<*>?,
                        view: View?,
                        position: Int,
                        id: Long
                    ) {

                    }

                    override fun onNothingSelected(parent: AdapterView<*>?) {

                    }

                }
            }

            cardSpinner = layout.findViewById<Spinner>(R.id.card_spinner).apply {
                adapter = IconAdapter(cards, { iconManager[type] }, Card::name)
                onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(
                        parent: AdapterView<*>?,
                        view: View?,
                        position: Int,
                        id: Long
                    ) {

                    }

                    override fun onNothingSelected(parent: AdapterView<*>?) {

                    }

                }
            }

            setPositiveButton(R.string.add_card) { dialog, id ->
                dialog.dismiss()
            }

            setNegativeButton(android.R.string.cancel) { dialog, _ ->
                dialog.cancel()
            }
        }.create()

    }


}
