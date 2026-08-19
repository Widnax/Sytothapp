package com.example.sytothapp.domain.engine

import com.example.sytothapp.data.local.entity.DailyEntry
import com.example.sytothapp.data.local.entity.MucusConsistency
import com.example.sytothapp.data.local.entity.MucusSensation
import com.example.sytothapp.domain.model.FertilityEvaluation
import com.example.sytothapp.domain.model.FertilityStatus
import java.time.LocalDate

class FertilityEngine {

    /**
     * Evaluates the fertility status for a list of entries, typically for a single cycle.
     * For simplicity, this implementation assumes the list is sorted by date.
     */
    fun evaluate(entries: List<DailyEntry>, targetDate: LocalDate): FertilityEvaluation {
        if (entries.isEmpty()) return FertilityEvaluation(FertilityStatus.UNKNOWN)

        val sortedEntries = entries.sortedBy { it.date }
        val (temperatureShiftDate, coverLine) = findTemperatureShiftWithCoverLine(sortedEntries)
        val mucusPeakDate = findMucusPeak(sortedEntries)

        // Basic STM Rule: Infertility is confirmed on the evening of the 3rd day 
        // after BOTH the temperature shift AND the mucus peak have occurred.
        
        val isTempConfirmed = temperatureShiftDate != null && !targetDate.isBefore(temperatureShiftDate.plusDays(2))
        val isMucusConfirmed = mucusPeakDate != null && !targetDate.isBefore(mucusPeakDate.plusDays(3))

        val isInfertile = isTempConfirmed && isMucusConfirmed

        return FertilityEvaluation(
            status = if (isInfertile) FertilityStatus.INFERTILE else FertilityStatus.FERTILE,
            isConfirmedTemperatureShift = isTempConfirmed,
            isConfirmedMucusPeak = isMucusConfirmed,
            coverLine = coverLine,
            temperatureShiftDate = temperatureShiftDate,
            peakDay = mucusPeakDate
        )
    }

    private fun findTemperatureShiftWithCoverLine(entries: List<DailyEntry>): Pair<LocalDate?, Double?> {
        if (entries.size < 9) return null to null // Need at least 6 low + 3 high

        for (i in 6 until entries.size - 2) {
            val lowSix = entries.subList(i - 6, i).mapNotNull { it.basalBodyTemperature }
            if (lowSix.size < 6) continue

            val coverLine = lowSix.maxOrNull() ?: continue
            
            val highOne = entries[i].basalBodyTemperature ?: continue
            val highTwo = entries[i + 1].basalBodyTemperature ?: continue
            val highThree = entries[i + 2].basalBodyTemperature ?: continue

            // Standard Rule: 3 temps above cover line, 3rd temp >= cover line + 0.2
            if (highOne > coverLine && highTwo > coverLine && highThree >= (coverLine + 0.2)) {
                return entries[i].date to coverLine
            }
        }
        return null to null
    }

    private fun findMucusPeak(entries: List<DailyEntry>): LocalDate? {
        var potentialPeak: LocalDate? = null
        
        for (i in entries.indices) {
            if (isHighlyFertileMucus(entries[i])) {
                potentialPeak = entries[i].date
            } else if (potentialPeak != null) {
                // Check if we have 3 days of drying up
                if (i + 2 < entries.size) {
                    val nextThree = entries.subList(i, i + 3)
                    if (nextThree.all { !isHighlyFertileMucus(it) && hasMucusData(it) }) {
                        return potentialPeak
                    }
                }
            }
        }
        return potentialPeak
    }

    private fun isHighlyFertileMucus(entry: DailyEntry): Boolean {
        return entry.mucusConsistency == MucusConsistency.EGG_WHITE ||
                entry.mucusSensation == MucusSensation.SLIPPERY ||
                entry.mucusSensation == MucusSensation.WET
    }

    private fun hasMucusData(entry: DailyEntry): Boolean {
        return entry.mucusConsistency != null || entry.mucusSensation != null
    }
}
