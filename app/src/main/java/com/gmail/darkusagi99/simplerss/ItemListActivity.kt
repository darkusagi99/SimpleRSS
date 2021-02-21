package com.gmail.darkusagi99.simplerss

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.text.Html
import android.view.*
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat.startActivity
import androidx.core.widget.ImageViewCompat
import androidx.core.widget.NestedScrollView
import androidx.recyclerview.widget.RecyclerView
import java.io.InputStream
import java.net.URI

/**
 * An activity representing a list of Pings. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
 * lead to a [ItemDetailActivity] representing
 * item details. On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 */
class ItemListActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val dbManager = FeedDatabase(this)
        dbManager.loadAllEntries()

        setContentView(R.layout.activity_item_list)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        toolbar.title = title

        setupRecyclerView(findViewById(R.id.item_list))
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)

        return super.onCreateOptionsMenu(menu)
    }

    @SuppressLint("ResourceType")
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.app_bar_feeds -> {
                startActivity(Intent(this, ItemFeedActivity::class.java))
            }
            R.id.app_bar_refresh -> {
                Toast.makeText(this.applicationContext, "Rafraîchissement", Toast.LENGTH_SHORT).show()
                FeedList.refreshEntries(this)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun setupRecyclerView(recyclerView: RecyclerView) {
        recyclerView.adapter = SimpleItemRecyclerViewAdapter(this, FeedList.ENTRIES)
    }

    class SimpleItemRecyclerViewAdapter(private val parentActivity: ItemListActivity,
                                        private val values: List<FeedList.FeedEntry>) :
            RecyclerView.Adapter<SimpleItemRecyclerViewAdapter.ViewHolder>() {

        private val onClickListener: View.OnClickListener

        init {
            onClickListener = View.OnClickListener { v ->
                val item = v.tag as FeedList.FeedEntry

                val intent = Intent(v.context, ItemDetailActivity::class.java).apply {
                    putExtra(ItemDetailFragment.ARG_ITEM_ID, item.link)
                }
                v.context.startActivity(intent)

            }
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
                FeedList.deleteItem(item.link, it.context)
                this.notifyDataSetChanged()
            }

            // Partage
            holder.shareButton.setOnClickListener {
                val shareIntent = Intent()
                val shareLink : String = item.link
                shareIntent.action = Intent.ACTION_SEND
                shareIntent.type = "text/plain"
                shareIntent.putExtra(Intent.EXTRA_TEXT, shareLink)
                startActivity(it.context, Intent.createChooser(shareIntent, shareLink), null)
            }


            // Ouvrir dans le navigateur
            holder.openButton.setOnClickListener {
                val entryBrowser = Intent(Intent.ACTION_VIEW)
                entryBrowser.data = Uri.parse(item.link)
                startActivity(it.context, entryBrowser, null)
            }

            with(holder.itemView) {
                tag = item
                setOnClickListener(onClickListener)
            }
        }

        override fun getItemCount() = values.size

        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            // Champs de l'entrée
            val titleView: TextView = view.findViewById(R.id.item_title)
            val contentView: TextView = view.findViewById(R.id.item_content)
            val contentEnclosure: ImageView = view.findViewById(R.id.item_enclosure)

            // Boutons
            val deleteButton : TextView = view.findViewById(R.id.button_delete)
            val shareButton : TextView = view.findViewById(R.id.button_share)
            val openButton : TextView = view.findViewById(R.id.button_link)
        }
    }
}