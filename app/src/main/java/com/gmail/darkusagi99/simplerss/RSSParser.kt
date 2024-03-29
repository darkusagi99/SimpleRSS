package com.gmail.darkusagi99.simplerss

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.os.StrictMode
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import org.xmlpull.v1.XmlPullParserFactory
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter


/**
 * Class to parse RSS entries from a Stream
 * */
class RSSParser {

    private var rssItem : FeedEntry ?= null
    private var text: String? = null

    fun refreshFeed(feedUrl: String, lastUpdate: Long, dbManager: FeedDatabase) {
        val stream: InputStream?

        val connectionUrl = URL(feedUrl)

        val sdkVersion = Build.VERSION.SDK_INT
        if (sdkVersion > 8) {
            val policy = StrictMode.ThreadPolicy.Builder()
                .permitAll().build()
            StrictMode.setThreadPolicy(policy)
        }

        val connect = connectionUrl.openConnection() as HttpURLConnection
        connect.readTimeout = 8000
        connect.connectTimeout = 8000
        connect.requestMethod = "GET"
        connect.connect()

        val responseCode: Int = connect.responseCode
        var updatedLastUpdate = lastUpdate

        if (responseCode == 200) {
            stream = connect.inputStream
            try {
                updatedLastUpdate = this.parse(stream!!, lastUpdate, dbManager)
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }

        // MAJ du chargement du dernier élément
        dbManager.updateFeed(feedUrl, updatedLastUpdate)

    }

    // Parse RSS Feed entries and return result List
    private fun parse(inputStream: InputStream, lastUpdate: Long, dbManager: FeedDatabase) : Long {
        try {
            val factory = XmlPullParserFactory.newInstance()
            factory.isNamespaceAware = true
            val parser = factory.newPullParser()
            parser.setInput(inputStream, null)
            var eventType = parser.eventType
            var foundItem = false
            var maxUpdateTime = lastUpdate
            var currentTime = 0L
            while (eventType != XmlPullParser.END_DOCUMENT) {
                val tagname = parser.name
                when (eventType) {
                    XmlPullParser.START_TAG -> if (tagname.equals("item", ignoreCase = true)) {
                        // create a new instance of entry
                        foundItem = true
                        rssItem = FeedEntry("", "", 0, "", "", null)
                    }
                    XmlPullParser.TEXT -> text = parser.text
                    XmlPullParser.END_TAG -> if (tagname.equals("item", ignoreCase = true)) {
                        // add entry object to list
                        if (currentTime > lastUpdate) {
                            rssItem!!.enclosureImage = getByteArrayFromURL(rssItem!!.imgLink)
                            dbManager.insertEntry(rssItem)
                        }
                        foundItem = false
                    } else if (foundItem && tagname.equals("title", ignoreCase = true)) {
                        rssItem!!.title = text.toString()
                    } else if (foundItem && tagname.equals("link", ignoreCase = true)) {
                        rssItem!!.link = text.toString()
                    } else if (foundItem && tagname.equals("pubDate", ignoreCase = true)) {
                        currentTime = extractDate(text.toString())
                        if (currentTime > maxUpdateTime) {
                            maxUpdateTime = currentTime
                        }
                        rssItem!!.pubDate = currentTime
                    } else if (foundItem && tagname.equals("enclosure", ignoreCase = true)) {
                        rssItem!!.imgLink = parser.getAttributeValue(null, "url")
                    } else if (foundItem && tagname.equals("description", ignoreCase = true)) {
                        rssItem!!.description = text.toString()
                    }
                }
                eventType = parser.next()
            }

            return maxUpdateTime
        } catch (e: XmlPullParserException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return lastUpdate
    }

    private fun getByteArrayFromURL(src: String?): ByteArray? {
        return try {
            val url = URL(src)
            val connection = url
                .openConnection() as HttpURLConnection
            connection.doInput = true
            connection.connect()
            val input = connection.inputStream
            val contentType = connection.getHeaderField("Content-Type")
            val isImage = contentType.startsWith("image/") // if img is true then it is an image

            return if (isImage) {
                input.readBytes()
            } else {
                null
            }

        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun extractDate(dateString: String) : Long {
        return LocalDateTime.from(DateTimeFormatter.RFC_1123_DATE_TIME.parse(dateString))?.toEpochSecond(ZoneOffset.UTC)!!
    }


}

