package com.example.sytothapp.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

enum class TemperatureUnit {
    CELSIUS, FAHRENHEIT
}

enum class ThemeMode {
    SYSTEM, LIGHT, DARK
}

class SettingsRepository(private val context: Context) {

    private val temperatureUnitKey = stringPreferencesKey("temperature_unit")
    private val themeModeKey = stringPreferencesKey("theme_mode")

    val temperatureUnit: Flow<TemperatureUnit> = context.dataStore.data.map { preferences ->
        val unitName = preferences[temperatureUnitKey] ?: TemperatureUnit.CELSIUS.name
        TemperatureUnit.valueOf(unitName)
    }

    val themeMode: Flow<ThemeMode> = context.dataStore.data.map { preferences ->
        val modeName = preferences[themeModeKey] ?: ThemeMode.SYSTEM.name
        ThemeMode.valueOf(modeName)
    }

    suspend fun setTemperatureUnit(unit: TemperatureUnit) {
        context.dataStore.edit { preferences ->
            preferences[temperatureUnitKey] = unit.name
        }
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        context.dataStore.edit { preferences ->
            preferences[themeModeKey] = mode.name
        }
    }
}
