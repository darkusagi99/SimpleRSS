package com.gmail.darkusagi99.simplerss

import java.util.*

object FeedConfig {

    /**
     * A map of feeds.
     */
    val FEED_MAP: MutableMap<String, FeedConfig.FeedItem> = HashMap()

    init {

    }

    fun addFeed(feedUrl : String) {
        val newFeed = FeedItem(feedUrl, Date(1L))
        FEED_MAP[feedUrl] = newFeed
    }

    fun removeFeed(feedUrl : String) {
        FEED_MAP.remove(feedUrl)
    }

    /**
     * A dummy item representing a piece of content.
     */
    data class FeedItem(val url: String, val lastUpdate: Date) {
        override fun toString(): String = url + " - " + lastUpdate.time
    }

}