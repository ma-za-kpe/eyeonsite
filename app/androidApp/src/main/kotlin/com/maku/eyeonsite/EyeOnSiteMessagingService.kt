package com.maku.eyeonsite

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class EyeOnSiteMessagingService : FirebaseMessagingService() {
    override fun onNewToken(token: String) {
        getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_FCM_TOKEN, token)
            .apply()

        // Sync to Firestore so Cloud Functions can reach the user via FCM
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        FirebaseFirestore
            .getInstance()
            .document("users/$uid")
            .update("fcmToken", token)
            .addOnFailureListener { e ->
                // Create the document if it doesn't exist yet
                FirebaseFirestore
                    .getInstance()
                    .document("users/$uid")
                    .set(mapOf("fcmToken" to token))
                    .addOnFailureListener { inner ->
                        android.util.Log.w(TAG, "FCM token sync failed", inner)
                    }
            }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        val title = message.notification?.title ?: message.data["title"] ?: return
        val body = message.notification?.body ?: message.data["body"] ?: ""
        val screen = message.data["screen"]
        showNotification(title, body, screen)
    }

    private fun showNotification(
        title: String,
        body: String,
        screen: String?,
    ) {
        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        ensureChannel(nm)

        val intent =
            Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
                screen?.let { putExtra(EXTRA_SCREEN, it) }
            }
        val pi =
            PendingIntent.getActivity(
                this,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )

        val notification =
            NotificationCompat
                .Builder(this, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle(title)
                .setContentText(body)
                .setAutoCancel(true)
                .setContentIntent(pi)
                .build()

        nm.notify(System.currentTimeMillis().toInt(), notification)
    }

    private fun ensureChannel(nm: NotificationManager) {
        if (nm.getNotificationChannel(CHANNEL_ID) != null) return
        nm.createNotificationChannel(
            NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT),
        )
    }

    companion object {
        private const val TAG = "EyeOnSiteMessaging"
        private const val PREFS_NAME = "eyeonsite_prefs"
        private const val KEY_FCM_TOKEN = "fcm_token"
        const val EXTRA_SCREEN = "screen"
        private const val CHANNEL_ID = "eyeonsite_main"
        private const val CHANNEL_NAME = "EyeOnSite Notifications"
    }
}
