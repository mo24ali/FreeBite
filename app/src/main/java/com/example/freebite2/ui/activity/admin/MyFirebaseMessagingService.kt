package com.example.freebite2.ui.activity.admin

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        if (remoteMessage.data.isNotEmpty()) {
            val title = remoteMessage.data["title"]
            val body = remoteMessage.data["body"]

            // Implement the logic to show the message in the notification fragment
            // showNotificationInFragment(title, body)
        }
    }

    private fun showNotificationInFragment(title: String?, body: String?) {
        // Implement this method to show the message in the notification fragment
    }
}
