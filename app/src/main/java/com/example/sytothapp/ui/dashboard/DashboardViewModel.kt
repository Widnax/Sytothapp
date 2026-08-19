package com.example.sytothapp.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.example.sytothapp.SytothApplication
import com.example.sytothapp.data.local.entity.DailyEntry
import com.example.sytothapp.data.repository.CycleRepository
import com.example.sytothapp.data.repository.SettingsRepository
import com.example.sytothapp.data.repository.TemperatureUnit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn

class DashboardViewModel(
    private val repository: CycleRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {
    val entries: Flow<List<DailyEntry>> = repository.getAllEntries()

    val temperatureUnit: StateFlow<TemperatureUnit> = settingsRepository.temperatureUnit
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), TemperatureUnit.CELSIUS)

    companion object {
        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
                val application = checkNotNull(extras[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]) as SytothApplication
                return DashboardViewModel(application.repository, application.settingsRepository) as T
            }
        }
    }
}
