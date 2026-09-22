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

data class SocialFeedItem(
    val id: String,
    val initials: String,
    val name: String,
    val statusText: String,
    val timeAgo: String,
    val flameCount: Int,
    val isRisk: Boolean = false
)

data class ChallengeInvite(
    val senderId: String,
    val receiverEmail: String,
    val challengeType: String,
    val durationDays: Int
)