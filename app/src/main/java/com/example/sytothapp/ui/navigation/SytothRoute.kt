package com.example.sytothapp.ui.navigation

import androidx.navigation3.runtime.NavKey
import com.example.sytothapp.data.local.entity.LocalDateSerializer
import kotlinx.serialization.Serializable
import java.time.LocalDate

@Serializable
sealed interface SytothRoute : NavKey {
    @Serializable
    data object Dashboard : SytothRoute

    @Serializable
    data class Logging(
        @Serializable(with = LocalDateSerializer::class)
        val date: LocalDate? = null
    ) : SytothRoute

    @Serializable
    data object Chart : SytothRoute
}
