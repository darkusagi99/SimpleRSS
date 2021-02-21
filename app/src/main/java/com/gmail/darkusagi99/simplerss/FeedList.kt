package com.gmail.darkusagi99.simplerss

import android.content.Context
import android.graphics.Bitmap
import java.util.HashMap
import java.util.logging.Logger

object FeedList {

    private val LOGGER : Logger = Logger.getGlobal()

    val ENTRIES : ArrayList<FeedEntry> = ArrayList()

    private val rssParser : RSSParser = RSSParser()

    /**
     * A map of sample (dummy) items, by ID.
     */
    val ENTRY_MAP: MutableMap<String, FeedEntry> = HashMap()


    init {
        // Add some sample items.

    }

    fun addItem(item: FeedEntry, context : Context) {

        val dbManager = FeedDatabase(context)
        dbManager.insertEntry(item)

        ENTRY_MAP[item.link] = item
        ENTRIES.add(item)

        LOGGER.info("Current list Size : " + ENTRIES.size)
    }

    fun deleteItem(itemLink : String, context: Context) {

        val dbManager = FeedDatabase(context)
        dbManager.deleteEntry(itemLink)


        val deleteEntry = ENTRY_MAP[itemLink]
        ENTRIES.remove(deleteEntry)
        ENTRY_MAP.remove(itemLink)

        LOGGER.info("Current list Size : " + ENTRIES.size)

    }

    // Method in order to refresh entries
    fun refreshEntries(context : Context) {

        // In order to refresh entries List, loop on Feeds and check them.
        LOGGER.info("Refresh Feeds")

        val dbManager = FeedDatabase(context)
        dbManager.loadAllFeeds()

        val feedCopy : List<FeedConfig.FeedItem> = FeedConfig.FEEDS.clone() as List<FeedConfig.FeedItem>

        // Loop on feeds
        for (currentFeed in feedCopy) {
            LOGGER.info("Refresh Feeds - Current : " + currentFeed.url)
            rssParser.refreshFeed(currentFeed.url, currentFeed.lastUpdate.time, context)
        }

    }

    fun initFeedEntry(): FeedEntry {
        return FeedEntry("", "", 0, "", "", null)
    }

    /**
     * A dummy item representing a piece of content.
     */
    data class FeedEntry(var link: String, var title: String?, var pubDate: Long?, var description: String?, var imgLink: String?, var enclosureImage: ByteArray?) {
        override fun toString(): String = link
    }

}