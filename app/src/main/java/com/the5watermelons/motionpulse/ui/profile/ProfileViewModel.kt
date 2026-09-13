package com.the5watermelons.motionpulse.ui.profile

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.the5watermelons.motionpulse.data.local.AppDatabase
import com.the5watermelons.motionpulse.data.local.HabitEntity
import com.the5watermelons.motionpulse.data.repository.HabitRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class ProfileViewModel(application: Application) : AndroidViewModel(application) {

    data class ProfileSummary(
        val dayStreak: Int = 0,
        val badgeCount: Int = 0,
        val recentAchievements: List<HabitEntity> = emptyList()
    )

    private val db = AppDatabase.getInstance(application)
    private val repository = HabitRepository(db.habitDao(), db.habitCompletionDao(), application.applicationContext)

    val summary: StateFlow<ProfileSummary> = repository.observeHabits()
        .map { habits ->
            ProfileSummary(
                dayStreak = habits.maxOfOrNull { it.streakCount } ?: 0,
                badgeCount = habits.count { it.streakCount > 0 },
                recentAchievements = habits
                    .filter { it.lastCompletedAt > 0 }
                    .sortedByDescending { it.lastCompletedAt }
                    .take(3)
            )
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ProfileSummary())
}