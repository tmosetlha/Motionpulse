package com.the5watermelons.motionpulse.ui.stats

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.the5watermelons.motionpulse.data.local.AppDatabase
import com.the5watermelons.motionpulse.data.local.HabitEntity
import com.the5watermelons.motionpulse.data.repository.HabitRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class StatsViewModel(application: Application) : AndroidViewModel(application) {

    data class HabitWeekly(val habit: HabitEntity, val weekDots: List<Boolean>)

    data class StatsSummary(
        val doneToday: Int = 0,
        val totalHabits: Int = 0,
        val currentStreak: Int = 0,
        val totalCompleted: Int = 0,
        val weekPercent: Int = 0
    )

    private val db = AppDatabase.getInstance(application)
    private val repository = HabitRepository(db.habitDao(), db.habitCompletionDao(), application.applicationContext)
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    private val habits: StateFlow<List<HabitEntity>> = repository.observeHabits()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _weeklyData = MutableStateFlow<List<HabitWeekly>>(emptyList())
    val weeklyData: StateFlow<List<HabitWeekly>> = _weeklyData

    private val _summary = MutableStateFlow(StatsSummary())
    val summary: StateFlow<StatsSummary> = _summary

    // --- MOOD FLOW ---
    // Represents the "Outcome" of your Mood Logs. 
    // Score mapping: Drained (0.2), Low (0.4), Steady (0.6), Good (0.8), Energized (1.0)
    private val _moodScores = MutableStateFlow<List<Float>>(listOf(0.6f, 0.4f, 0.7f, 0.3f, 0.6f, 0.2f, 0.8f))
    val moodScores: StateFlow<List<Float>> = _moodScores

    init {
        viewModelScope.launch {
            habits.collectLatest { list -> refresh(list) }
        }
    }

    private fun currentWeekDates(): List<String> {
        val calendar = Calendar.getInstance()
        // Roll back to this week's Monday (Calendar.DAY_OF_WEEK: Sun=1..Sat=7)
        val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
        val daysSinceMonday = (dayOfWeek + 5) % 7
        calendar.add(Calendar.DAY_OF_YEAR, -daysSinceMonday)

        return (0..6).map { offset ->
            val cal = calendar.clone() as Calendar
            cal.add(Calendar.DAY_OF_YEAR, offset)
            dateFormat.format(cal.time)
        }
    }

    private suspend fun refresh(list: List<HabitEntity>) {
        val weekDates = currentWeekDates()
        val startDate = weekDates.first()
        val endDate = weekDates.last()

        var totalCompletedAllTime = 0
        var weekTrueCount = 0

        val weekly = list.map { habit ->
            val completedDates = repository.completedDatesInRange(habit.id, startDate, endDate)
            val dots = weekDates.map { it in completedDates }
            weekTrueCount += dots.count { it }
            totalCompletedAllTime += repository.totalCompletionsForHabit(habit.id)
            HabitWeekly(habit, dots)
        }
        _weeklyData.value = weekly

        val totalHabits = list.size
        val doneToday = list.count { it.isDoneToday }
        val currentStreak = list.maxOfOrNull { it.streakCount } ?: 0
        val weekPercent = if (totalHabits == 0) 0 else (weekTrueCount * 100) / (totalHabits * 7)

        _summary.value = StatsSummary(
            doneToday = doneToday,
            totalHabits = totalHabits,
            currentStreak = currentStreak,
            totalCompleted = totalCompletedAllTime,
            weekPercent = weekPercent
        )
    }

    /**
     * Updates the mood score for today in the view state.
     * This is the bridge between your "Log Mood" button and the Stats Chart.
     */
    fun updateTodayMoodScore(moodLevel: String) {
        val score = when (moodLevel) {
            "Energized" -> 1.0f
            "Good" -> 0.8f
            "Steady" -> 0.6f
            "Low" -> 0.4f
            "Drained" -> 0.2f
            else -> 0.6f
        }
        
        val currentScores = _moodScores.value.toMutableList()
        // Update the last item (today)
        if (currentScores.isNotEmpty()) {
            currentScores[currentScores.size - 1] = score
            _moodScores.value = currentScores
        }
    }
}