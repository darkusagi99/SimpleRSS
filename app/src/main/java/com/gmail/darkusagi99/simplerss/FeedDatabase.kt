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
    var colUrl = "URL"
    var colLastUpdate = "LastUpdate"
    var colTitle = "Title"
    var colPubDate = "PubDate"
    var colDescription = "Description"
    var colImgLink = "ImageLink"
    var colImgData = "ImageData"
    //database version
    var dbVersion = 1

    //CREATE TABLE IF NOT EXISTS MyNotes (ID INTEGER PRIMARY KEY,title TEXT, Description TEXT);"
    val sqlCreateFeedTable = "CREATE TABLE IF NOT EXISTS $dbFeedTable ($colUrl TEXT PRIMARY KEY,$colLastUpdate INTEGER);"
    val sqlCreateEntriesTable = "CREATE TABLE IF NOT EXISTS $dbEntriesTable ($colUrl TEXT PRIMARY KEY,$colTitle TEXT,$colPubDate INTEGER,$colDescription TEXT,$colImgLink TEXT,$colImgData BLOB);"

    var sqlDB: SQLiteDatabase? = null

    init {
        val db = DatabaseHelperFeeds(context)
        sqlDB = db.writableDatabase
    }

    inner class DatabaseHelperFeeds(context: Context) : SQLiteOpenHelper(context, dbName, null, dbVersion) {

        override fun onCreate(db: SQLiteDatabase?) {
            db!!.execSQL(sqlCreateFeedTable)
            db.execSQL(sqlCreateEntriesTable)
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

        val ID = sqlDB!!.insert(dbFeedTable, "", values)
        return ID
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

        return feedList;
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


    fun insertEntry(newEntry: FeedEntry): Long {

        var imageData = byteArrayOf()
        if (newEntry.enclosureImage != null) {
            imageData = newEntry.enclosureImage!!
        }

        val values = ContentValues()
        values.put(colUrl , newEntry.link)
        values.put(colTitle, newEntry.title)
        values.put(colPubDate, newEntry.pubDate)
        values.put(colDescription, newEntry.description)
        values.put(colImgLink, newEntry.imgLink)
        values.put(colImgData, imageData)

        // Suppression de l'entrée avant ré-insertion
        val deleteSelection = "$colUrl = ?"
        val deleteSelectionArgs = arrayOf(newEntry.link)
        sqlDB!!.delete(dbEntriesTable, deleteSelection, deleteSelectionArgs)

        val ID = sqlDB!!.insert(dbEntriesTable, "", values)
        return ID
    }

    fun loadAllEntries() : ArrayList<FeedEntry> {

        val feedEntries = ArrayList<FeedEntry>()
        val qb = SQLiteQueryBuilder()
        qb.tables = dbEntriesTable

        val projections = arrayOf(colUrl, colTitle, colPubDate, colDescription, colImgLink, colImgData)
        val cursor =  qb.query(sqlDB, projections, null, null, null, null, colUrl)

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
        return feedEntries
    }

    fun deleteEntry(entryUrl : String): Int {
        val selection = "$colUrl = ?"
        val selectionArgs = arrayOf(entryUrl)
        return sqlDB!!.delete(dbEntriesTable, selection, selectionArgs)
    }

}