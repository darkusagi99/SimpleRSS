package com.gmail.darkusagi99.simplerss

import android.app.AlertDialog
import android.content.DialogInterface
import android.database.sqlite.SQLiteDatabase
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
import java.util.*
import kotlin.collections.ArrayList

/**
 * An activity representing a list of Pings. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
 * lead to a [ItemDetailActivity] representing
 * item details. On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 */
class ItemFeedActivity : AppCompatActivity() {

    private var feedList : ArrayList<FeedItem> = ArrayList()
    private var dbManager: FeedDatabase ? = null
    private var feedViewAdapter : FeedViewAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dbManager = FeedDatabase(this)
        feedList = dbManager!!.loadAllFeeds()
        feedViewAdapter = FeedViewAdapter(feedList, dbManager!!)

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
                            Toast.makeText(taskEditText.context, "Création - " + taskEditText.text.toString(), Toast.LENGTH_SHORT).show()
                            dbManager!!.insertFeed(taskEditText.text.toString())
                            feedList.add(FeedItem(taskEditText.text.toString(),  Date(1)))
                            feedViewAdapter!!.updateValues(feedList)
                        }
                        DialogInterface.BUTTON_NEGATIVE -> {
                            Toast.makeText(taskEditText.context, "Pas de Création - " + taskEditText.text.toString(), Toast.LENGTH_SHORT).show()
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

    override fun onResume() {
        super.onResume()

        feedList = dbManager!!.loadAllFeeds()
    }

    private fun setupRecyclerView(recyclerView: RecyclerView) {
        recyclerView.adapter = feedViewAdapter
    }
}