package com.gmail.darkusagi99.simplerss

import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton

/**
 * An activity representing a list of Pings. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
 * lead to a [ItemDetailActivity] representing
 * item details. On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 */
class ItemFeedActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val dbManager = FeedDatabase(this)
        dbManager.loadAllFeeds()

        setContentView(R.layout.activity_feed_list)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        toolbar.title = title

        findViewById<FloatingActionButton>(R.id.fab).setOnClickListener { view ->

            val taskEditText =  EditText(view.context)
            val dialogClickListener =
                DialogInterface.OnClickListener { _, which ->
                    when (which) {
                        DialogInterface.BUTTON_POSITIVE -> {
                            Toast.makeText(this, "Création - " + taskEditText.text.toString(), Toast.LENGTH_SHORT).show()
                            FeedConfig.addFeed(taskEditText.text.toString(), this)
                        }
                        DialogInterface.BUTTON_NEGATIVE -> {
                            Toast.makeText(this, "Pas de Création - " + taskEditText.text.toString(), Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            val ab: AlertDialog.Builder = AlertDialog.Builder(view.context)
            ab.setMessage("Ajouter nouveau flux RSS ?")
                .setView(taskEditText)
                .setPositiveButton("Oui", dialogClickListener)
                .setNegativeButton("Non", dialogClickListener).show()
        }

        setupRecyclerView(findViewById(R.id.feed_list))
    }

    private fun setupRecyclerView(recyclerView: RecyclerView) {
        recyclerView.adapter = SimpleItemRecyclerViewAdapter(FeedConfig.FEEDS)
    }

    class SimpleItemRecyclerViewAdapter(private val values: List<FeedConfig.FeedItem>) :
            RecyclerView.Adapter<SimpleItemRecyclerViewAdapter.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.feed_list_content, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = values[position]
            holder.idView.text = item.url

            holder.deleteButton.setOnClickListener {
                FeedConfig.removeFeed(item.url, it.context)
                this.notifyDataSetChanged()
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
}