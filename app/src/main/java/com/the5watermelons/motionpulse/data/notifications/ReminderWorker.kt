package com.the5watermelons.motionpulse.data.notifications

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

class ReminderWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        NotificationHelper.showNotification(
            applicationContext,
            title = "Time to build your rhythm \uD83D\uDC9F",
            body = "Don't forget to check off today's habits in Motion.Pulse"
        )
        return Result.success()
    }
}