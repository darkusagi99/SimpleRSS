package com.gmail.darkusagi99.simplerss

import java.util.*

data class FeedItem(val url: String, var lastUpdate: Date) {
    override fun toString(): String = url + " - " + lastUpdate.time
}