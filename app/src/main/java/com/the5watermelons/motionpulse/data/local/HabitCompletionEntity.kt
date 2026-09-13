package com.the5watermelons.motionpulse.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * One row per habit per day it was completed. Powers the weekly M-T-W-T-F-S-S
 * consistency dots on the Stats screen and lifetime completion totals.
 */
@Entity(tableName = "habit_completions")
data class HabitCompletionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val habitId: String,
    // Stored as "yyyy-MM-dd" so a habit can only be logged once per calendar day.
    val dateString: String
)