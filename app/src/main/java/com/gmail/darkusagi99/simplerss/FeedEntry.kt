package com.gmail.darkusagi99.simplerss

data class FeedEntry(var link: String, var title: String?, var pubDate: Long?, var description: String?, var imgLink: String?, var enclosureImage: ByteArray?) {
    override fun toString(): String = link
}
