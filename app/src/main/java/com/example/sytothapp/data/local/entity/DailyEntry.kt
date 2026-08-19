package com.example.sytothapp.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable
import java.time.LocalDate

@Entity(tableName = "daily_entries")
@Serializable
data class DailyEntry(
    @PrimaryKey
    @Serializable(with = LocalDateSerializer::class)
    val date: LocalDate,
    val basalBodyTemperature: Double?,
    val mucusConsistency: MucusConsistency?,
    val mucusSensation: MucusSensation?,
    val disturbances: List<String> = emptyList(),
    val cervicalPosition: CervicalPosition? = null,
    val cervicalFirmness: CervicalFirmness? = null,
    val cervicalOpening: CervicalOpening? = null,
    val notes: String = ""
)

@Serializable
enum class MucusConsistency {
    NONE, DRY, STICKY, CREAMY, EGG_WHITE
}

@Serializable
enum class MucusSensation {
    NONE, DRY, MOIST, WET, SLIPPERY
}

@Serializable
enum class CervicalPosition {
    LOW, MEDIUM, HIGH
}

@Serializable
enum class CervicalFirmness {
    FIRM, MEDIUM, SOFT
}

@Serializable
enum class CervicalOpening {
    CLOSED, MEDIUM, OPEN
}
