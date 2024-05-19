package com.example.freebite2.data

import android.content.ContentValues
import com.example.freebite2.model.*
import android.content.Context
import com.google.firebase.database.*

class DatabaseRepository(context: Context) {
    private val dbHelper = DatabaseHelper(context)
    private val db = dbHelper.writableDatabase
    private val firebaseDatabase: FirebaseDatabase = FirebaseDatabase.getInstance()
    private val usersRef: DatabaseReference = firebaseDatabase.getReference("users")
    private val offersRef: DatabaseReference = firebaseDatabase.getReference("offers")
    private val reviewsRef: DatabaseReference = firebaseDatabase.getReference("reviews")
    private val chatRef: DatabaseReference = firebaseDatabase.getReference("chat")

    // User operations
    fun insertUser(user: User) {
        val values = ContentValues().apply {
            put("uid", user.uid)
            put("firstName", user.firstName)
            put("lastName", user.lastName)
            put("email", user.email)
            put("profilePictureUrl", user.profilePictureUrl)
            put("rating", user.rating)
            put("ratingCount", user.ratingCount)
        }
        db.insert("users", null, values)
        usersRef.child(user.uid).setValue(user)
    }

    fun readUsersFromSQLite(): List<User> {
        val projection = arrayOf("uid", "firstName", "lastName", "email", "profilePictureUrl", "rating", "ratingCount")
        val cursor = db.query("users", projection, null, null, null, null, null)
        val dataList = mutableListOf<User>()
        with(cursor) {
            while (moveToNext()) {
                val uid = getString(getColumnIndexOrThrow("uid"))
                val firstName = getString(getColumnIndexOrThrow("firstName"))
                val lastName = getString(getColumnIndexOrThrow("lastName"))
                val email = getString(getColumnIndexOrThrow("email"))
                val profilePictureUrl = getString(getColumnIndexOrThrow("profilePictureUrl"))
                val rating = getFloat(getColumnIndexOrThrow("rating"))
                val ratingCount = getInt(getColumnIndexOrThrow("ratingCount"))
                dataList.add(User(uid, firstName, lastName, email, profilePictureUrl, rating, ratingCount,))
            }
            close()
        }
        return dataList
    }

    // Offer operations
    fun insertOffer(offer: OffreModel) {
        val values = ContentValues().apply {
            put("userID", offer.userID)
            put("offerID", offer.offerID)
            put("details", offer.details)
            put("distance", offer.distance)
            put("latitude", offer.latitude)
            put("longitude", offer.longitude)
            put("imageUrls", offer.imageUrls?.joinToString(","))
            put("timestamp", offer.timestamp)
            put("status", offer.status)
        }
        db.insert("offers", null, values)
        offer.offerID?.let { offersRef.child(it).setValue(offer) }
    }

    fun readOffersFromSQLite(): List<OffreModel> {
        val projection = arrayOf("userID", "offerID", "details", "distance", "latitude", "longitude", "imageUrls", "timestamp", "status")
        val cursor = db.query("offers", projection, null, null, null, null, null)
        val dataList = mutableListOf<OffreModel>()
        with(cursor) {
            while (moveToNext()) {
                val userID = getString(getColumnIndexOrThrow("userID"))
                val offerID = getString(getColumnIndexOrThrow("offerID"))
                val details = getString(getColumnIndexOrThrow("details"))
                val distance = getDouble(getColumnIndexOrThrow("distance"))
                val latitude = getDouble(getColumnIndexOrThrow("latitude"))
                val longitude = getDouble(getColumnIndexOrThrow("longitude"))
                val imageUrls = getString(getColumnIndexOrThrow("imageUrls"))?.split(",")
                val timestamp = getLong(getColumnIndexOrThrow("timestamp"))
                val status = getString(getColumnIndexOrThrow("status"))
                dataList.add(OffreModel(userID, offerID, details, distance, latitude, longitude, imageUrls, timestamp, status))
            }
            close()
        }
        return dataList
    }

    // Review operations
    fun insertReview(review: Review) {
        val values = ContentValues().apply {
            put("reviewID", review.reviewID)
            put("reviewerID", review.reviewerID)
            put("revieweeID", review.revieweeID)
            put("rating", review.rating)
            put("comment", review.comment)
            put("timestamp", review.timestamp)
        }
        db.insert("reviews", null, values)
        review.reviewID.let { reviewsRef.child(it).setValue(review) }
    }

    fun readReviewsFromSQLite(): List<Review> {
        val projection = arrayOf("reviewID", "reviewerID", "revieweeID", "rating", "comment", "timestamp")
        val cursor = db.query("reviews", projection, null, null, null, null, null)
        val dataList = mutableListOf<Review>()
        with(cursor) {
            while (moveToNext()) {
                val reviewID = getString(getColumnIndexOrThrow("reviewID"))
                val reviewerID = getString(getColumnIndexOrThrow("reviewerID"))
                val revieweeID = getString(getColumnIndexOrThrow("revieweeID"))
                val rating = getFloat(getColumnIndexOrThrow("rating"))
                val comment = getString(getColumnIndexOrThrow("comment"))
                val timestamp = getLong(getColumnIndexOrThrow("timestamp"))
                dataList.add(Review(reviewID, reviewerID, revieweeID, rating, comment, timestamp))
            }
            close()
        }
        return dataList
    }

    // Chat operations
    fun insertChatMessage(chatMessage: ChatMessage) {
        val values = ContentValues().apply {
            put("messageID", chatMessage.messageID)
            put("senderID", chatMessage.senderID)
            put("receiverID", chatMessage.receiverID)
            put("message", chatMessage.message)
            put("timestamp", chatMessage.timestamp)
        }
        db.insert("chat", null, values)
        chatMessage.messageID.let { chatRef.child(it).setValue(chatMessage) }
    }

    fun readChatMessagesFromSQLite(): List<ChatMessage> {
        val projection = arrayOf("messageID", "senderID", "receiverID", "message", "timestamp")
        val cursor = db.query("chat", projection, null, null, null, null, null)
        val dataList = mutableListOf<ChatMessage>()
        with(cursor) {
            while (moveToNext()) {
                val messageID = getString(getColumnIndexOrThrow("messageID"))
                val senderID = getString(getColumnIndexOrThrow("senderID"))
                val receiverID = getString(getColumnIndexOrThrow("receiverID"))
                val message = getString(getColumnIndexOrThrow("message"))
                val timestamp = getLong(getColumnIndexOrThrow("timestamp"))
                dataList.add(ChatMessage(messageID, senderID, receiverID, message, timestamp))
            }
            close()
        }
        return dataList
    }

    // Sync with Firebase for all tables
    // Similar to offers sync example previously provided
}
