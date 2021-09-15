package com.gmail.darkusagi99.simplerss

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.database.sqlite.SQLiteQueryBuilder
import androidx.core.database.getBlobOrNull
import java.util.*
import kotlin.collections.ArrayList

class FeedDatabase(context: Context) {

    //database name
    var dbName = "RSS"
    //table name
    var dbFeedTable = "FEEDS"
    var dbEntriesTable = "ENTRIES"
    //columns
    private var colUrl = "URL"
    private var colLastUpdate = "LastUpdate"
    private var colTitle = "Title"
    private var colPubDate = "PubDate"
    private var colDescription = "Description"
    private var colImgLink = "ImageLink"
    private var colImgData = "ImageData"
    //database version
    var dbVersion = 3

    //CREATE TABLE IF NOT EXISTS MyNotes (ID INTEGER PRIMARY KEY,title TEXT, Description TEXT);"
    val sqlCreateFeedTable = "CREATE TABLE IF NOT EXISTS $dbFeedTable ($colUrl TEXT PRIMARY KEY,$colLastUpdate INTEGER);"
    val sqlCreateEntriesTable = "CREATE TABLE IF NOT EXISTS $dbEntriesTable ($colUrl TEXT PRIMARY KEY,$colTitle TEXT,$colPubDate INTEGER,$colDescription TEXT,$colImgLink TEXT,$colImgData BLOB);"

    private var sqlDB: SQLiteDatabase? = null

    init {
        val db = DatabaseHelperFeeds(context)
        sqlDB = db.writableDatabase
    }

    inner class DatabaseHelperFeeds(context: Context) : SQLiteOpenHelper(context, dbName, null, dbVersion) {

        override fun onCreate(db: SQLiteDatabase?) {
            db!!.execSQL(sqlCreateFeedTable)
            db.execSQL(sqlCreateEntriesTable)

            // Init fields
            initInsertFeed(db, "https://korben.info/feed")
            initInsertFeed(db,"https://www.francetvinfo.fr/titres.rss")
            initInsertFeed(db, "https://www.futura-sciences.com/rss/actualites.xml")

        }

        private fun initInsertFeed(db: SQLiteDatabase?, feedUrl: String): Long {

            val values = ContentValues()
            values.put(colUrl , feedUrl)
            values.put(colLastUpdate, 1)

            return db!!.insert(dbFeedTable, "", values)
        }

        override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
            db!!.execSQL("Drop table if Exists $dbFeedTable")
            db.execSQL("Drop table if Exists $dbEntriesTable")
            onCreate(db)
        }

        override fun onDowngrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
            onUpgrade(db, oldVersion, newVersion)
        }

    }

    fun insertFeed(feedUrl: String): Long {

        val values = ContentValues()
        values.put(colUrl , feedUrl)
        values.put(colLastUpdate, 1)

        return sqlDB!!.insert(dbFeedTable, "", values)
    }

    fun  loadAllFeeds() : ArrayList<FeedItem> {
        val feedList = ArrayList<FeedItem>()
        val qb = SQLiteQueryBuilder()
        qb.tables = dbFeedTable

        val projections = arrayOf(colUrl, colLastUpdate)
        val cursor =  qb.query(sqlDB, projections, null, null, null, null, colUrl)

        if (cursor.moveToFirst()) {

            do {
                val url = cursor.getString(cursor.getColumnIndex(colUrl))
                val lastUpdateInt = cursor.getInt(cursor.getColumnIndex(colLastUpdate))
                val lastUpdateDate = Date(lastUpdateInt.toLong())

                val newFeed = FeedItem(url, lastUpdateDate)
                feedList.add(newFeed)

            } while (cursor.moveToNext())
        }
        cursor.close()

        return feedList
    }

    fun deleteFeed(urlFeed : String): Int {
        val selection = "$colUrl = ?"
        val selectionArgs = arrayOf(urlFeed)
        return sqlDB!!.delete(dbFeedTable, selection, selectionArgs)
    }

    fun updateFeed(feedUrl: String, feedUpdateDate: Long): Int {

        val values = ContentValues()
        values.put(colUrl , feedUrl)
        values.put(colLastUpdate, feedUpdateDate)

        val selection = "$colUrl = ?"
        val selectionArgs = arrayOf(feedUrl)

        return sqlDB!!.update(dbFeedTable, values, selection, selectionArgs)
    }


    fun insertEntry(newEntry: FeedEntry?): Long {

        var imageData = byteArrayOf()
        if (newEntry!!.enclosureImage != null) {
            imageData = newEntry.enclosureImage!!
        }

        val values = ContentValues()
        values.put(colUrl , newEntry.link)
        values.put(colTitle, newEntry.title)
        values.put(colPubDate, newEntry.pubDate)
        values.put(colDescription, newEntry.description)
        values.put(colImgLink, newEntry.imgLink)
        values.put(colImgData, imageData)


        val projections = arrayOf(colUrl, colTitle, colPubDate, colDescription, colImgLink, colImgData)
        val qb = SQLiteQueryBuilder()
        qb.tables = dbEntriesTable

        val selection = "$colUrl = ?"
        val selectionArgs = arrayOf(newEntry.link)
        val cursor =  qb.query(sqlDB, projections, selection, selectionArgs, null, null, colUrl)
        cursor.count

        var id = 0L
        if (cursor.count == 0) {
            id = sqlDB!!.insert(dbEntriesTable, "", values)
        }
        cursor.close()

        return id
    }

    fun loadAllEntries() : ArrayList<FeedEntry> {

        val feedEntries = ArrayList<FeedEntry>()
        val qb = SQLiteQueryBuilder()
        qb.tables = dbEntriesTable

        val cursor =  qb.query(sqlDB, null, null, null, null, null, colUrl)

        if (cursor.moveToFirst()) {

            do {
                val url = cursor.getString(cursor.getColumnIndex(colUrl))
                val title = cursor.getString(cursor.getColumnIndex(colTitle))
                val pubDateInt = cursor.getLong(cursor.getColumnIndex(colPubDate))
                val description = cursor.getString(cursor.getColumnIndex(colDescription))
                val imageUrl = cursor.getString(cursor.getColumnIndex(colImgLink))
                val imageData = cursor.getBlobOrNull(cursor.getColumnIndex(colImgData))

                val newEntry = FeedEntry(url, title, pubDateInt, description, imageUrl, imageData)
                feedEntries.add(newEntry)

            } while (cursor.moveToNext())
        }
        cursor.close()
        return feedEntries
    }

    fun deleteEntry(entryUrl : String): Int {
        val selection = "$colUrl = ?"
        val selectionArgs = arrayOf(entryUrl)
        return sqlDB!!.delete(dbEntriesTable, selection, selectionArgs)
    }

}