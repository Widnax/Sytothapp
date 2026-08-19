package com.example.sytothapp.domain.model

import java.time.LocalDate

enum class FertilityStatus {
    FERTILE,
    INFERTILE,
    UNKNOWN
}

data class FertilityEvaluation(
    val status: FertilityStatus,
    val isConfirmedTemperatureShift: Boolean = false,
    val isConfirmedMucusPeak: Boolean = false,
    val coverLine: Double? = null,
    val temperatureShiftDate: LocalDate? = null,
    val peakDay: LocalDate? = null
)
