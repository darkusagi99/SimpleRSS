package com.gmail.darkusagi99.simplerss

import android.content.Context
import java.util.HashMap
import java.util.logging.Logger

object FeedList {

    val LOGGER : Logger = Logger.getGlobal()

    val ENTRIES : ArrayList<FeedEntry> = ArrayList<FeedEntry>()

    val rssParser : RSSParser = RSSParser()

    /**
     * A map of sample (dummy) items, by ID.
     */
    val ENTRY_MAP: MutableMap<String, FeedEntry> = HashMap()

    private val COUNT = 25

    init {
        // Add some sample items.

    }

    fun addItem(item: FeedEntry) {
        ENTRY_MAP[item.link] = item
        ENTRIES.add(item)
    }

    // Method in order to refresh entries
    fun refreshEntries(context : Context) {

        // In order to refresh entries List, loop on Feeds and check them.
        LOGGER.info("Refresh Feeds")

        val dbManager = FeedDatabase(context)
        dbManager.loadAllFeeds()

        // Loop on feeds
        for (currentFeed in FeedConfig.FEEDS) {
            LOGGER.info("Refresh Feeds - Current : " + currentFeed.url)
            rssParser.refreshFeed(currentFeed.url, currentFeed.lastUpdate)
        }

    }

    fun initFeedEntry(): FeedList.FeedEntry {
        return FeedEntry("", "", "", "", "")
    }

    /**
     * A dummy item representing a piece of content.
     */
    data class FeedEntry(var link: String, var title: String, var pubDate: String, var category: String, var description : String) {
        override fun toString(): String = link
    }

}