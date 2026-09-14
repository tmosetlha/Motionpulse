package com.the5watermelons.motionpulse.data.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.the5watermelons.motionpulse.R

object NotificationHelper {

    const val CHANNEL_ID = "motionpulse_reminders"
    private const val CHANNEL_NAME = "Habit Reminders"

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Reminders to complete your daily habits"
            }
            val manager = context.getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }

    fun showNotification(context: Context, title: String, body: String, notificationId: Int = 1001) {
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            // This is the ceiling for notification customization -- Android
            // renders the notification shade itself, so a full gradient
            // background (like the in-app messages) isn't possible here.
            // setColor tints the small icon and app name text only.
            .setColor(androidx.core.content.ContextCompat.getColor(context, R.color.mp_pink))
            .build()

        // Caller is responsible for having checked POST_NOTIFICATIONS permission
        // on API 33+ before reaching this point.
        runCatching {
            NotificationManagerCompat.from(context).notify(notificationId, notification)
        }
    }
}