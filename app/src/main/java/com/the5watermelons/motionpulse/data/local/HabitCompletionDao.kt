package com.the5watermelons.motionpulse.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface HabitCompletionDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(completion: HabitCompletionEntity)

    @Query("DELETE FROM habit_completions WHERE habitId = :habitId AND dateString = :dateString")
    suspend fun deleteForDate(habitId: String, dateString: String)

    @Query("SELECT COUNT(*) FROM habit_completions WHERE habitId = :habitId")
    suspend fun countForHabit(habitId: String): Int

    @Query(
        "SELECT * FROM habit_completions WHERE habitId = :habitId " +
                "AND dateString BETWEEN :startDate AND :endDate"
    )
    fun observeForHabitInRange(
        habitId: String,
        startDate: String,
        endDate: String
    ): Flow<List<HabitCompletionEntity>>

    @Query(
        "SELECT dateString FROM habit_completions WHERE habitId = :habitId " +
                "AND dateString BETWEEN :startDate AND :endDate"
    )
    suspend fun getCompletedDatesInRange(
        habitId: String,
        startDate: String,
        endDate: String
    ): List<String>
}