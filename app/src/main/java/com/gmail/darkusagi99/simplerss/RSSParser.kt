package com.gmail.darkusagi99.simplerss

import android.os.Build
import android.os.StrictMode
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import org.xmlpull.v1.XmlPullParserFactory
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.*


/**
 * Class to parse RSS entries from a Stream
 * */
class RSSParser {

    private var rssItem : FeedList.FeedEntry ?= null
    private var text: String? = null

    fun refreshFeed(feedUrl : String, lastUpdate : Date) {
        var stream: InputStream?

        val connectionUrl = URL(feedUrl)

        val SDK_INT = Build.VERSION.SDK_INT
        if (SDK_INT > 8) {
            val policy = StrictMode.ThreadPolicy.Builder()
                .permitAll().build()
            StrictMode.setThreadPolicy(policy)
        }

        val connect = connectionUrl.openConnection() as HttpURLConnection
        connect.readTimeout = 8000
        connect.connectTimeout = 8000
        connect.requestMethod = "GET"
        connect.connect();

        val responseCode: Int = connect.responseCode;

        if (responseCode == 200) {
            stream = connect.inputStream;
            try {
                this.parse(stream!!)
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    // Parse RSS Feed entries and return result List
    private fun parse(inputStream: InputStream) {
        try {
            val factory = XmlPullParserFactory.newInstance()
            factory.isNamespaceAware = true
            val parser = factory.newPullParser()
            parser.setInput(inputStream, null)
            var eventType = parser.eventType
            var foundItem = false
            while (eventType != XmlPullParser.END_DOCUMENT) {
                val tagname = parser.name
                when (eventType) {
                    XmlPullParser.START_TAG -> if (tagname.equals("item", ignoreCase = true)) {
                        // create a new instance of entry
                        foundItem = true
                        rssItem = FeedList.initFeedEntry()
                    }
                    XmlPullParser.TEXT -> text = parser.text
                    XmlPullParser.END_TAG -> if (tagname.equals("item", ignoreCase = true)) {
                        // add entry object to list
                        rssItem?.let { FeedList.addItem(it) }
                        foundItem = false
                    } else if ( foundItem && tagname.equals("title", ignoreCase = true)) {
                        rssItem!!.title = text.toString()
                    } else if (foundItem && tagname.equals("link", ignoreCase = true)) {
                        rssItem!!.link = text.toString()
                    } else if (foundItem && tagname.equals("pubDate", ignoreCase = true)) {
                        rssItem!!.pubDate = text.toString()
                    } else if (foundItem && tagname.equals("category", ignoreCase = true)) {
                        rssItem!!.category = text.toString()
                    } else if (foundItem && tagname.equals("description", ignoreCase = true)) {
                        rssItem!!.description = text.toString()
                    }
                }
                eventType = parser.next()
            }
        } catch (e: XmlPullParserException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }


}