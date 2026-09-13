package com.the5watermelons.motionpulse.data.sync

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.firebase.firestore.FirebaseFirestore
import com.the5watermelons.motionpulse.data.local.AppDatabase
import kotlinx.coroutines.tasks.await

/**
 * Sweeps up any habit still marked syncStatus = false (written while offline)
 * and pushes it to Firestore. Runs both as a one-off retry (triggered right
 * after a failed write, once connectivity returns) and as a periodic safety
 * net, so nothing gets permanently stuck unsynced.
 */
class SyncWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            val habitDao = AppDatabase.getInstance(applicationContext).habitDao()
            val firestore = FirebaseFirestore.getInstance()
            val unsynced = habitDao.getUnsyncedHabits()

            if (unsynced.isEmpty()) return Result.success()

            unsynced.forEach { habit ->
                firestore.collection("users").document(habit.userId)
                    .collection("habits").document(habit.id)
                    .set(habit)
                    .await()
                habitDao.update(habit.copy(syncStatus = true))
            }

            Result.success()
        } catch (e: Exception) {
            // Still offline or Firestore unreachable -- let WorkManager retry
            // later (it backs off automatically) rather than losing the work.
            Result.retry()
        }
    }

    companion object {
        const val WORK_NAME_ONE_TIME = "motionpulse_sync_one_time"
        const val WORK_NAME_PERIODIC = "motionpulse_sync_periodic"
    }
}