package com.mktiti.treachery

import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import kotlinx.android.synthetic.main.icon_item.view.*

class IconAdapter<T>(
    private val values: List<T>,
    //private val selectCallback: (T) -> Unit,
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
/*
            itemView.setOnClickListener {
                selectCallback(this)
            }

 */
        }

        return itemView
    }

}
/*
class IconAdapter<T>(
    private val values: List<T>,
    private val selectCallback: (T) -> Unit,
    private val iconMapper: T.() -> Drawable,
    private val textMapper: T.() -> String
) : RecyclerView.Adapter<IconAdapter.Holder>(), ListAdapter {

    class Holder(view: View) : RecyclerView.ViewHolder(view) {
        val icon: ImageView = view.findViewById(R.id.icon)
        val title: TextView = view.findViewById(R.id.title)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.icon_item, parent, false)
        return Holder(itemView)
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        with(values[position]) {
            holder.icon.setImageDrawable(iconMapper())
            holder.title.text = textMapper()

            holder.itemView.setOnClickListener {
                selectCallback(this)
            }
        }
    }

    override fun getItemCount() = values.size

}
 */