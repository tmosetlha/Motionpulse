package com.the5watermelons.motionpulse.api

data class MoodLogPayload(
    val userId: String,
    val moodLevel: String,
    val factors: List<String>,
    val note: String,
    val timestamp: Long = System.currentTimeMillis()
)

data class MoodStreakResponse(
    val streakCount: Int,
    val lastLoggedDate: String
)