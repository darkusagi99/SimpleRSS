package com.gmail.darkusagi99.simplerss

import android.content.Context
import android.webkit.URLUtil
import java.util.*
import java.util.logging.Logger
import kotlin.collections.ArrayList
import kotlin.collections.List
import kotlin.collections.MutableMap
import kotlin.collections.remove
import kotlin.collections.set

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

    fun addItem(item: FeedEntry, dbManager : FeedDatabase) {

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
    fun refreshEntries(dbManager: FeedDatabase) {

        // In order to refresh entries List, loop on Feeds and check them.
        LOGGER.info("Refresh Feeds")

        dbManager.loadAllFeeds()

        val feedCopy : List<FeedItem> = dbManager.loadAllFeeds()

        // Loop on feeds
        for (currentFeed in feedCopy) {
            if (URLUtil.isValidUrl(currentFeed.url)) {
                LOGGER.info("Refresh Feeds - Current : " + currentFeed.url)
                rssParser.refreshFeed(currentFeed.url, currentFeed.lastUpdate.time, dbManager)
            } else {
                LOGGER.warning("Invalid URL")
            }
        }

    }

    fun initFeedEntry(): FeedEntry {
        return FeedEntry("", "", 0, "", "", null)
    }

}