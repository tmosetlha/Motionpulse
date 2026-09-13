package com.the5watermelons.motionpulse.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.the5watermelons.motionpulse.data.local.HabitCompletionDao
import com.the5watermelons.motionpulse.data.local.HabitCompletionEntity
import com.the5watermelons.motionpulse.data.local.HabitDao
import com.the5watermelons.motionpulse.data.local.HabitEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * Room is the single source of truth for the UI (offline-first). Firestore is
 * written to opportunistically alongside every local write; if that write
 * fails (no connectivity), the record stays in Room with syncStatus = false.
 * A dedicated WorkManager job (added in a later step) sweeps up anything still
 * unsynced once connectivity returns.
 */
class HabitRepository(
    private val habitDao: HabitDao,
    private val completionDao: HabitCompletionDao,
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    private val currentUserId: String
        get() = FirebaseAuth.getInstance().currentUser?.uid ?: "anonymous"

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    fun observeHabits(): Flow<List<HabitEntity>> =
        habitDao.getHabitsForUser(currentUserId)

    fun observeCompletionsInRange(habitId: String, startDate: String, endDate: String): Flow<List<HabitCompletionEntity>> =
        completionDao.observeForHabitInRange(habitId, startDate, endDate)

    suspend fun totalCompletionsForHabit(habitId: String): Int =
        completionDao.countForHabit(habitId)

    suspend fun completedDatesInRange(habitId: String, startDate: String, endDate: String): List<String> =
        completionDao.getCompletedDatesInRange(habitId, startDate, endDate)

    suspend fun addHabit(name: String, category: String) {
        val habit = HabitEntity(
            userId = currentUserId,
            name = name,
            category = category
        )
        habitDao.insert(habit)
        pushToFirestore(habit)
    }

    suspend fun toggleComplete(habit: HabitEntity) {
        val nowDone = !habit.isDoneToday
        val today = dateFormat.format(System.currentTimeMillis())

        val updated = habit.copy(
            isDoneToday = nowDone,
            streakCount = if (nowDone) habit.streakCount + 1 else maxOf(0, habit.streakCount - 1),
            lastCompletedAt = if (nowDone) System.currentTimeMillis() else habit.lastCompletedAt,
            syncStatus = false
        )
        habitDao.update(updated)

        if (nowDone) {
            completionDao.insert(HabitCompletionEntity(habitId = habit.id, dateString = today))
        } else {
            completionDao.deleteForDate(habit.id, today)
        }

        pushToFirestore(updated)
    }

    suspend fun deleteHabit(habit: HabitEntity) {
        habitDao.delete(habit)
        runCatching {
            firestore.collection("users").document(currentUserId)
                .collection("habits").document(habit.id)
                .delete()
                .await()
        }
    }

    private suspend fun pushToFirestore(habit: HabitEntity) {
        runCatching {
            firestore.collection("users").document(currentUserId)
                .collection("habits").document(habit.id)
                .set(habit)
                .await()
            habitDao.update(habit.copy(syncStatus = true))
        }
        // Failure is expected when offline -- syncStatus simply stays false,
        // and the record remains fully usable locally via Room.
    }
}