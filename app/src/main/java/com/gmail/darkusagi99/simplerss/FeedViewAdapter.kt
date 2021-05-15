package com.gmail.darkusagi99.simplerss

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class FeedViewAdapter (private var values: ArrayList<FeedItem>, dbManager : FeedDatabase) :
        RecyclerView.Adapter<FeedViewAdapter.ViewHolder>() {


    private var dbManager: FeedDatabase? = null

    init {
        this.dbManager = dbManager
    }

    fun updateValues(newValues: ArrayList<FeedItem>) {
        values = newValues
        this.notifyDataSetChanged()
        this.notifyItemRangeChanged(0, values.size)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.feed_list_content, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = values[position]
        holder.idView.text = item.url
        holder.deleteButton.setOnClickListener {

            dbManager?.deleteFeed(item.url)
            values.removeAt(position)
            this.notifyItemRemoved(position)
            this.notifyItemRangeChanged(0, values.size)
        }

        with(holder.itemView) {
            tag = item
        }
    }

    override fun getItemCount() = values.size

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val idView: TextView = view.findViewById(R.id.id_text)
        val deleteButton: Button = view.findViewById(R.id.delete_button)
    }

}