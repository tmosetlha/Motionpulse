package com.the5watermelons.motionpulse.data.notifications

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

/**
 * Receives real FCM pushes. Testable right now via Firebase Console ->
 * Engage -> Messaging -> "Send test message" using this device's token
 * (logged below). Server-triggered sends (e.g. "streak about to break")
 * require a Cloud Functions backend to actually call the FCM API -- this
 * class is the client-side half of that pipeline, ready for it.
 */
class MotionPulseMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        val title = message.notification?.title ?: "Motion.Pulse"
        val body = message.notification?.body ?: "You have a new update"

        NotificationHelper.showNotification(applicationContext, title, body)
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        // A future step (once a backend exists) would upload this token to
        // Firestore under the user's document so a Cloud Function knows where
        // to send that user's notifications.
        android.util.Log.d("MotionPulseFCM", "New FCM token: $token")
    }
}