package com.example.freebite2.data


import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "freebite.db"
        private const val DATABASE_VERSION = 1

        private const val USERS_TABLE_CREATE =
            "CREATE TABLE users (" +
                    "uid TEXT PRIMARY KEY, " +
                    "firstName TEXT, " +
                    "lastName TEXT, " +
                    "email TEXT, " +
                    "profilePictureUrl TEXT, " +
                    "phoneNumber TEXT, " +
                    "latitude REAL, " +
                    "longitude REAL, " +
                    "rating REAL, " +
                    "ratingCount INTEGER);"

        private const val OFFERS_TABLE_CREATE =
            "CREATE TABLE offers (" +
                    "userID TEXT, " +
                    "offerID TEXT PRIMARY KEY, " +
                    "details TEXT, " +
                    "distance REAL, " +
                    "latitude REAL, " +
                    "longitude REAL, " +
                    "imageUrls TEXT, " + // JSON string
                    "timestamp INTEGER, " +
                    "status TEXT);"

        private const val REVIEWS_TABLE_CREATE =
            "CREATE TABLE reviews (" +
                    "reviewID TEXT PRIMARY KEY, " +
                    "reviewerID TEXT, " +
                    "revieweeID TEXT, " +
                    "rating REAL, " +
                    "comment TEXT, " +
                    "timestamp INTEGER);"

        private const val CHAT_TABLE_CREATE =
            "CREATE TABLE chat (" +
                    "messageID TEXT PRIMARY KEY, " +
                    "senderID TEXT, " +
                    "receiverID TEXT, " +
                    "message TEXT, " +
                    "timestamp INTEGER);"
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(USERS_TABLE_CREATE)
        db.execSQL(OFFERS_TABLE_CREATE)
        db.execSQL(REVIEWS_TABLE_CREATE)
        db.execSQL(CHAT_TABLE_CREATE)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS users")
        db.execSQL("DROP TABLE IF EXISTS offers")
        db.execSQL("DROP TABLE IF EXISTS reviews")
        db.execSQL("DROP TABLE IF EXISTS chat")
        onCreate(db)
    }
}
