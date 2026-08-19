package com.example.sytothapp

import android.app.Application
import com.example.sytothapp.data.local.SytothDatabase
import com.example.sytothapp.data.repository.CycleRepository
import com.example.sytothapp.data.repository.SettingsRepository

class SytothApplication : Application() {
    val database: SytothDatabase by lazy { SytothDatabase.getDatabase(this) }
    val repository: CycleRepository by lazy { CycleRepository(database.dailyEntryDao()) }
    val settingsRepository: SettingsRepository by lazy { SettingsRepository(this) }
}
