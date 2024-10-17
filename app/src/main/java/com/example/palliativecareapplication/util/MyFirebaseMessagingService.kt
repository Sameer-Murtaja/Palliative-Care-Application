package com.example.palliativecareapplication.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.example.palliativecareapplication.R
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService() {
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        // Extract notification data
        val title = remoteMessage.data["title"]
        val body = remoteMessage.data["body"]

        Log.e("TAG", "onMessageReceived: $title", )
        Log.e("TAG", "onMessageReceived: $body", )

        // Create notification channel (required for Android Oreo and above)
        createNotificationChannel()

        // Build the notification
        val notificationBuilder = NotificationCompat.Builder(this, "topicsNotifications")
            .setContentTitle(title)
            .setContentText(body)
            .setSmallIcon(R.drawable.ic_email)
            .setAutoCancel(true)

        // Show the notification
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(0, notificationBuilder.build())
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelId = "topicsNotifications"
            val channelName = "topics Notifications"
            val channelDescription = "Notifications about topics"

            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(channelId, channelName, importance).apply {
                description = channelDescription
            }

            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.e("TAG", "onNewToken: $token", )

    }
}