package com.example.freebite2.ui.activity.admin

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class PushNotificationsService: FirebaseMessagingService() {
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        // send the new token to the server
        //update the server
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        //respond to received messages

    }
}