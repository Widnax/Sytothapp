package com.example.sytothapp

import android.app.Application
import com.example.sytothapp.data.local.SytothDatabase
import com.example.sytothapp.data.repository.CycleRepository

class SytothApplication : Application() {
    val database: SytothDatabase by lazy { SytothDatabase.getDatabase(this) }
    val repository: CycleRepository by lazy { CycleRepository(database.dailyEntryDao()) }
}
