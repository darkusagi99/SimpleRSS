package com.gmail.darkusagi99.simplerss

import android.content.ContentValues
import android.content.Context
import java.util.*

object FeedConfig {

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
    }

    fun removeFeed(feedUrl : String, context : Context) {
        val dbManager = FeedDatabase(context)

        dbManager.deleteFeed(feedUrl)
        FEED_MAP.remove(feedUrl)
    }

    /**
     * A dummy item representing a piece of content.
     */
    data class FeedItem(val url: String, val lastUpdate: Date) {
        override fun toString(): String = url + " - " + lastUpdate.time
    }

}