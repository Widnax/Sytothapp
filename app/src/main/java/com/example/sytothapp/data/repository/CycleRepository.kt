package com.example.sytothapp.data.repository

import com.example.sytothapp.data.local.dao.DailyEntryDao
import com.example.sytothapp.data.local.entity.DailyEntry
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

class CycleRepository(private val dailyEntryDao: DailyEntryDao) {

    fun getAllEntries(): Flow<List<DailyEntry>> = dailyEntryDao.getAllEntriesFlow()

    fun getEntriesInRange(startDate: LocalDate, endDate: LocalDate): Flow<List<DailyEntry>> =
        dailyEntryDao.getEntriesInRange(startDate, endDate)

    suspend fun getEntryForDate(date: LocalDate): DailyEntry? = dailyEntryDao.getEntryByDate(date)

    suspend fun upsertEntry(entry: DailyEntry) {
        dailyEntryDao.insertEntry(entry)
    }

    suspend fun deleteEntry(entry: DailyEntry) {
        dailyEntryDao.deleteEntry(entry)
    }

    suspend fun deleteEntryByDate(date: LocalDate) {
        dailyEntryDao.deleteEntryByDate(date)
    }
}
