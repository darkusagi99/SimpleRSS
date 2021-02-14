package com.gmail.darkusagi99.simplerss

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.database.sqlite.SQLiteQueryBuilder
import android.widget.Toast
import java.util.*

class FeedDatabase(context: Context) {

    //database name
    var dbName = "RSS"
    //table name
    var dbFeedTable = "FEEDS"
    var dbEntriesTable = "ENTRIES"
    //columns
    var colUrl = "URL"
    var colLastUpdate = "LastUpdate"
    //database version
    var dbVersion = 1

    //CREATE TABLE IF NOT EXISTS MyNotes (ID INTEGER PRIMARY KEY,title TEXT, Description TEXT);"
    val sqlCreateFeedTable = "CREATE TABLE IF NOT EXISTS $dbFeedTable ($colUrl TEXT PRIMARY KEY,$colLastUpdate INTEGER);"

    var sqlDB: SQLiteDatabase? = null

    init {
        var db = DatabaseHelperFeeds(context)
        sqlDB = db.writableDatabase
    }

    inner class DatabaseHelperFeeds(context: Context) : SQLiteOpenHelper(context, dbName, null, dbVersion) {
        var context: Context? = context

        override fun onCreate(db: SQLiteDatabase?) {
            db!!.execSQL(sqlCreateFeedTable)
            Toast.makeText(this.context, "database created...", Toast.LENGTH_SHORT).show()
        }

        override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
            db!!.execSQL("Drop table if Exists$sqlCreateFeedTable")
        }


    }

    fun insertFeed(feedUrl: String): Long {

        val values = ContentValues()
        values.put(colUrl , feedUrl)
        values.put(colLastUpdate, 1)

        val ID = sqlDB!!.insert(dbFeedTable, "", values)
        return ID
    }

    fun loadAllFeeds() {
        val qb = SQLiteQueryBuilder();
        qb.tables = dbFeedTable

        val projections = arrayOf(colUrl, colLastUpdate)
        val cursor =  qb.query(sqlDB, projections, null, null, null, null, colUrl)

        FeedConfig.FEED_MAP.clear()
        FeedConfig.FEEDS.clear()
        if (cursor.moveToFirst()) {

            do {
                val url = cursor.getString(cursor.getColumnIndex(colUrl))
                val lastUpdateInt = cursor.getInt(cursor.getColumnIndex(colLastUpdate))
                val lastUpdateDate = Date(lastUpdateInt.toLong())

                val newFeed = FeedConfig.FeedItem(url, lastUpdateDate)
                FeedConfig.FEED_MAP[url] = newFeed
                FeedConfig.FEEDS.add(newFeed)

            } while (cursor.moveToNext())
        }
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

}