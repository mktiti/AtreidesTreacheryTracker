package com.mktiti.treachery.ui

import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import com.mktiti.treachery.R
import kotlinx.android.synthetic.main.icon_item.view.*

class IconAdapter<T>(
    private val values: List<T>,
    private val iconMapper: T.() -> Drawable,
    private val textMapper: T.() -> String
) : BaseAdapter() {

    override fun getCount() = values.size

    override fun getItem(position: Int) = values[position]

    override fun getItemId(position: Int) = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val itemView = convertView ?: LayoutInflater.from(parent.context).inflate(R.layout.icon_item, parent, false)

        with(values[position]) {
            itemView.findViewById<ImageView>(R.id.icon).icon.setImageDrawable(iconMapper())
            itemView.findViewById<TextView>(R.id.title).text = textMapper()
        }

        return itemView
    }

}