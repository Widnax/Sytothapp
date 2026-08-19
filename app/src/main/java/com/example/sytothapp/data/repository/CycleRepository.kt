package com.example.sytothapp.data.repository

import com.example.sytothapp.data.local.dao.DailyEntryDao
import com.example.sytothapp.data.local.entity.*
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

    suspend fun seedSampleData() {
        val today = LocalDate.now()
        for (i in 29 downTo 0) {
            val date = today.minusDays(i.toLong())
            val cycleDay = 30 - i // 1 to 30
            
            val entry = when {
                cycleDay <= 5 -> DailyEntry(
                    date = date,
                    basalBodyTemperature = 36.4,
                    mucusConsistency = null,
                    mucusSensation = null,
                    isPeriod = true,
                    flow = if (cycleDay <= 3) FlowLevel.HEAVY else FlowLevel.LIGHT
                )
                cycleDay <= 13 -> DailyEntry(
                    date = date,
                    basalBodyTemperature = 36.4,
                    mucusConsistency = if (cycleDay <= 9) MucusConsistency.DRY else MucusConsistency.STICKY,
                    mucusSensation = if (cycleDay <= 9) MucusSensation.DRY else MucusSensation.MOIST,
                    isPeriod = false
                )
                cycleDay == 14 -> DailyEntry(
                    date = date,
                    basalBodyTemperature = 36.4,
                    mucusConsistency = MucusConsistency.CREAMY,
                    mucusSensation = MucusSensation.WET,
                    isPeriod = false
                )
                cycleDay == 15 -> DailyEntry(
                    date = date,
                    basalBodyTemperature = 36.4,
                    mucusConsistency = MucusConsistency.EGG_WHITE,
                    mucusSensation = MucusSensation.SLIPPERY,
                    isPeriod = false
                )
                cycleDay == 16 -> DailyEntry(
                    date = date,
                    basalBodyTemperature = 36.5,
                    mucusConsistency = MucusConsistency.EGG_WHITE,
                    mucusSensation = MucusSensation.SLIPPERY,
                    isPeriod = false
                )
                else -> DailyEntry(
                    date = date,
                    basalBodyTemperature = 36.7 + (Math.random() * 0.1),
                    mucusConsistency = MucusConsistency.DRY,
                    mucusSensation = MucusSensation.DRY,
                    isPeriod = false
                )
            }
            upsertEntry(entry)
        }
    }
}
