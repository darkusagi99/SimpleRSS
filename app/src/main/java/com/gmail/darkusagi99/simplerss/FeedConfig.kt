package com.gmail.darkusagi99.simplerss

import android.content.Context
import java.util.*
import kotlin.collections.ArrayList

object FeedConfig {

    val FEEDS : ArrayList<FeedItem> = ArrayList()

    /**
     * A map of feeds.
     */
    val FEED_MAP: MutableMap<String, FeedItem> = HashMap()

    init {
    }

    fun addFeed(feedUrl : String, context : Context) {
        val newFeed = FeedItem(feedUrl, Date(1L))
        val dbManager = FeedDatabase(context)

        dbManager.insertFeed(feedUrl)
        FEED_MAP[feedUrl] = newFeed
        FEEDS.add(newFeed)
    }

    fun removeFeed(feedUrl : String, context : Context) {
        val dbManager = FeedDatabase(context)

        dbManager.deleteFeed(feedUrl)
        val deleteFeed = FEED_MAP[feedUrl]
        FEEDS.remove(deleteFeed)
        FEED_MAP.remove(feedUrl)
    }

    fun updateFeed(feedUrl: String, lastUpdate: Long, context : Context) {
        val dbManager = FeedDatabase(context)
        val newUpdateDate = Date(lastUpdate)

        val newFeed = FeedItem(feedUrl, newUpdateDate)

        dbManager.updateFeed(feedUrl, lastUpdate)

        val deleteFeed = FEED_MAP[feedUrl]
        FEEDS.remove(deleteFeed)
        FEED_MAP.remove(feedUrl)
        FEED_MAP[feedUrl] = newFeed
        FEEDS.add(newFeed)

    }

    /**
     * A dummy item representing a piece of content.
     */
    data class FeedItem(val url: String, var lastUpdate: Date) {
        override fun toString(): String = url + " - " + lastUpdate.time
    }

}