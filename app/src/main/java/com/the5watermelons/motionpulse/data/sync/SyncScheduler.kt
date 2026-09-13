package com.the5watermelons.motionpulse.data.sync

import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

object SyncScheduler {

    private val networkConstraint = Constraints.Builder()
        .setRequiredNetworkType(NetworkType.CONNECTED)
        .build()

    /**
     * Call this right after any Firestore write fails (e.g. from
     * HabitRepository) so the retry fires as soon as connectivity returns,
     * instead of waiting for the next periodic tick.
     */
    fun scheduleImmediateRetry(context: Context) {
        val request = OneTimeWorkRequestBuilder<SyncWorker>()
            .setConstraints(networkConstraint)
            .setBackoffCriteria(BackoffPolicy.LINEAR, 30, TimeUnit.SECONDS)
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            SyncWorker.WORK_NAME_ONE_TIME,
            ExistingWorkPolicy.KEEP,
            request
        )
    }

    /**
     * Call once from the Application class -- a safety net that sweeps for
     * any still-unsynced records every 15 minutes (WorkManager's minimum
     * periodic interval), in case a one-time retry was ever missed (e.g. app
     * was killed before connectivity returned).
     */
    fun schedulePeriodicSync(context: Context) {
        val request = PeriodicWorkRequestBuilder<SyncWorker>(15, TimeUnit.MINUTES)
            .setConstraints(networkConstraint)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            SyncWorker.WORK_NAME_PERIODIC,
            ExistingPeriodicWorkPolicy.KEEP,
            request
        )
    }
}