package com.gmail.darkusagi99.simplerss

import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.ActionBar
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView

class ItemViewAdapter(private var values: ArrayList<FeedEntry>, dbManager: FeedDatabase, supportActionBar: ActionBar?) :
        RecyclerView.Adapter<ItemViewAdapter.ViewHolder>() {

    var dbManager: FeedDatabase? = null
    var supportActionBar: ActionBar? = null

    init {
        this.dbManager = dbManager
        this.supportActionBar = supportActionBar
    }

    fun updateValues(newValues: ArrayList<FeedEntry>) {
        values = newValues;
        this.notifyDataSetChanged()
        this.notifyItemRangeChanged(0, values.size)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_list_content, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = values[position]

        // Textes de l'item
        holder.titleView.text = item.title
        holder.contentView.text = Html.fromHtml(item.description, Html.FROM_HTML_OPTION_USE_CSS_COLORS)

        // Image d'illustration
        val enclosureImage = BitmapFactory.decodeStream(item.enclosureImage?.inputStream())
        holder.contentEnclosure.setImageBitmap(enclosureImage)

        // Contrôle des boutons
        // Suppression
        holder.deleteButton.setOnClickListener {
            dbManager!!.deleteEntry(item.link)
            values.removeAt(position)
            this.notifyItemRemoved(position)
            this.notifyItemRangeChanged(0, values.size)

            //actionbar
            val total = values.size
            if (supportActionBar != null) {
                //set to actionbar as subtitle of actionbar
                supportActionBar!!.title = "$total entrée(s)"
            }
        }

        // Partage
        holder.shareButton.setOnClickListener {
            val shareIntent = Intent()
            val shareLink : String = item.link
            shareIntent.action = Intent.ACTION_SEND
            shareIntent.type = "text/plain"
            shareIntent.putExtra(Intent.EXTRA_TEXT, shareLink)
            ContextCompat.startActivity(it.context, Intent.createChooser(shareIntent, shareLink), null)
        }


        // Ouvrir dans le navigateur
        holder.openButton.setOnClickListener {
            val entryBrowser = Intent(Intent.ACTION_VIEW)
            entryBrowser.data = Uri.parse(item.link)
            ContextCompat.startActivity(it.context, entryBrowser, null)
        }

    }

    override fun getItemCount() = values.size

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        // Champs de l'entrée
        val titleView: TextView = view.findViewById(R.id.item_title)
        val contentView: TextView = view.findViewById(R.id.item_content)
        val contentEnclosure: ImageView = view.findViewById(R.id.item_enclosure)

        // Boutons
        val deleteButton : ImageButton = view.findViewById(R.id.button_delete)
        val shareButton : ImageButton = view.findViewById(R.id.button_share)
        val openButton : ImageButton = view.findViewById(R.id.button_link)
    }
}