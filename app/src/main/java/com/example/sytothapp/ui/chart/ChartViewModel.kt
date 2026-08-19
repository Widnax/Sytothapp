package com.example.sytothapp.ui.chart

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.example.sytothapp.SytothApplication
import com.example.sytothapp.data.local.entity.DailyEntry
import com.example.sytothapp.data.repository.CycleRepository
import com.example.sytothapp.domain.engine.FertilityEngine
import com.example.sytothapp.domain.model.FertilityEvaluation
import com.example.sytothapp.domain.model.FertilityStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.time.LocalDate

data class ChartUiState(
    val entries: List<DailyEntry> = emptyList(),
    val evaluation: FertilityEvaluation = FertilityEvaluation(FertilityStatus.UNKNOWN),
    val dailyStatuses: List<FertilityStatus> = emptyList()
)

class ChartViewModel(
    private val repository: CycleRepository,
    private val engine: FertilityEngine = FertilityEngine()
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChartUiState())
    val uiState: StateFlow<ChartUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.getAllEntries().collectLatest { entries ->
                val sortedEntries = entries.sortedBy { it.date }
                val lastDate = sortedEntries.lastOrNull()?.date ?: LocalDate.now()
                val evaluation = engine.evaluate(sortedEntries, lastDate)
                
                val dailyStatuses = sortedEntries.map { entry ->
                    engine.evaluate(sortedEntries, entry.date).status
                }
                
                _uiState.value = ChartUiState(sortedEntries, evaluation, dailyStatuses)
            }
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
                val application = checkNotNull(extras[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]) as SytothApplication
                return ChartViewModel(application.repository) as T
            }
        }
    }
}
