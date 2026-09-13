package com.the5watermelons.motionpulse.ui.habits

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.the5watermelons.motionpulse.data.local.AppDatabase
import com.the5watermelons.motionpulse.data.local.HabitEntity
import com.the5watermelons.motionpulse.data.repository.HabitRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Shared across Home and Habits tabs (via activityViewModels) so both screens
 * reflect the exact same live habit list without duplicating queries.
 */
class HabitsViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)
    private val repository: HabitRepository = HabitRepository(
        db.habitDao(),
        db.habitCompletionDao(),
        application.applicationContext
    )

    val habits: StateFlow<List<HabitEntity>> = repository.observeHabits()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addHabit(name: String, category: String) {
        viewModelScope.launch { repository.addHabit(name, category) }
    }

    fun toggleComplete(habit: HabitEntity) {
        viewModelScope.launch { repository.toggleComplete(habit) }
    }

    fun deleteHabit(habit: HabitEntity) {
        viewModelScope.launch { repository.deleteHabit(habit) }
    }
}