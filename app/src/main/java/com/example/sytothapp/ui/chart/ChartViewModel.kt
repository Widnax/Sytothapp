package com.example.sytothapp.ui.chart

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.example.sytothapp.SytothApplication
import com.example.sytothapp.data.local.entity.DailyEntry
import com.example.sytothapp.data.repository.CycleRepository
import com.example.sytothapp.data.repository.SettingsRepository
import com.example.sytothapp.data.repository.TemperatureUnit
import com.example.sytothapp.domain.engine.FertilityEngine
import com.example.sytothapp.domain.model.FertilityEvaluation
import com.example.sytothapp.domain.model.FertilityStatus
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate

data class ChartUiState(
    val entries: List<DailyEntry> = emptyList(),
    val evaluation: FertilityEvaluation = FertilityEvaluation(FertilityStatus.UNKNOWN),
    val dailyStatuses: List<FertilityStatus> = emptyList(),
    val temperatureUnit: TemperatureUnit = TemperatureUnit.CELSIUS
)

class ChartViewModel(
    private val repository: CycleRepository,
    private val settingsRepository: SettingsRepository,
    private val engine: FertilityEngine = FertilityEngine()
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChartUiState())
    val uiState: StateFlow<ChartUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                repository.getAllEntries(),
                settingsRepository.temperatureUnit
            ) { entries, unit ->
                val sortedEntries = entries.sortedBy { it.date }
                val lastDate = sortedEntries.lastOrNull()?.date ?: LocalDate.now()
                val evaluation = engine.evaluate(sortedEntries, lastDate)
                
                val dailyStatuses = sortedEntries.map { entry ->
                    engine.evaluate(sortedEntries, entry.date).status
                }
                
                ChartUiState(sortedEntries, evaluation, dailyStatuses, unit)
            }.collect { state ->
                _uiState.value = state
            }
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
                val application = checkNotNull(extras[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]) as SytothApplication
                return ChartViewModel(application.repository, application.settingsRepository) as T
            }
        }
    }
}
