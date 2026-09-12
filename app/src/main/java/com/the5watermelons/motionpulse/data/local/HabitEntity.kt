package com.the5watermelons.motionpulse.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "habits")
data class HabitEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val userId: String,
    val name: String,
    val category: String,
    val isDoneToday: Boolean = false,
    val streakCount: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val lastCompletedAt: Long = 0L,
    // Offline-first sync flag: false until this record has been confirmed
    // written to Firestore. A future WorkManager job pushes any record where
    // this is still false once the device has connectivity again.
    val syncStatus: Boolean = false
)