package com.gmail.darkusagi99.simplerss

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.webkit.URLUtil
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.RecyclerView

/**
 * An activity representing a list of Feed contents
 */
class ItemListActivity : AppCompatActivity() {


    private var feedItems : ArrayList<FeedEntry> = ArrayList()
    private var dbManager: FeedDatabase ? = null
    private var itemViewAdapter : ItemViewAdapter? = null

    private val rssParser : RSSParser = RSSParser()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        dbManager = FeedDatabase(this)
        feedItems = dbManager!!.loadAllEntries()

        itemViewAdapter = ItemViewAdapter(feedItems, dbManager!!, supportActionBar)

        setContentView(R.layout.activity_item_list)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        toolbar.title = title

        setupRecyclerView(findViewById(R.id.item_list))

        //actionbar
        updateActionBar()
        itemViewAdapter!!.updateActionBar(supportActionBar)

    }

    private fun updateActionBar() {
        //actionbar
        val total = feedItems.size
        val mActionBar = supportActionBar
        if (mActionBar != null) {
            //set to actionbar as subtitle of actionbar
            mActionBar.title = "$total entrée(s)"
        }
    }

    override fun onResume() {
        super.onResume()

        feedItems = dbManager!!.loadAllEntries()
        itemViewAdapter!!.updateActionBar(supportActionBar)
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
                Toast.makeText(this, "Rafraîchissement", Toast.LENGTH_SHORT).show()
                refreshEntries()
                Toast.makeText(this, "Rafraîchissement terminé", Toast.LENGTH_SHORT).show()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun setupRecyclerView(recyclerView: RecyclerView) {
        recyclerView.adapter = itemViewAdapter
    }

    // Method in order to refresh entries
    private fun refreshEntries() {

        // In order to refresh entries List, loop on Feeds and check them.
        val feedCopy : List<FeedItem> = dbManager!!.loadAllFeeds()

        // Loop on feeds
        for (currentFeed in feedCopy) {
            if (URLUtil.isValidUrl(currentFeed.url)) {
                rssParser.refreshFeed(currentFeed.url, currentFeed.lastUpdate.time, dbManager!!)
            }
        }

        // Rechargement de l'affichage
        feedItems = dbManager!!.loadAllEntries()
        itemViewAdapter!!.updateValues(feedItems)

        //actionbar
        updateActionBar()
    }

}