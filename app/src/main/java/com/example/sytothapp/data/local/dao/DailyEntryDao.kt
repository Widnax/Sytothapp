package com.example.sytothapp.data.local.dao

import androidx.room.*
import com.example.sytothapp.data.local.entity.DailyEntry
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface DailyEntryDao {
    @Query("SELECT * FROM daily_entries ORDER BY date DESC")
    fun getAllEntriesFlow(): Flow<List<DailyEntry>>

    @Query("SELECT * FROM daily_entries WHERE date = :date")
    suspend fun getEntryByDate(date: LocalDate): DailyEntry?

    @Query("SELECT * FROM daily_entries WHERE date BETWEEN :startDate AND :endDate ORDER BY date ASC")
    fun getEntriesInRange(startDate: LocalDate, endDate: LocalDate): Flow<List<DailyEntry>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntry(entry: DailyEntry)

    @Delete
    suspend fun deleteEntry(entry: DailyEntry)

    @Query("DELETE FROM daily_entries WHERE date = :date")
    suspend fun deleteEntryByDate(date: LocalDate)

    @Query("DELETE FROM daily_entries")
    suspend fun deleteAll()
}
