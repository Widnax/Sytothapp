package com.example.sytothapp.ui.logging

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.example.sytothapp.SytothApplication
import com.example.sytothapp.data.local.entity.*
import com.example.sytothapp.data.repository.CycleRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate

class LoggingViewModel(private val repository: CycleRepository) : ViewModel() {

    private val _selectedDate = MutableStateFlow(LocalDate.now())
    val selectedDate: StateFlow<LocalDate> = _selectedDate.asStateFlow()

    private val _entry = MutableStateFlow<DailyEntry?>(null)
    val entry: StateFlow<DailyEntry?> = _entry.asStateFlow()

    private var originalDate: LocalDate? = null

    private val _isExistingEntry = MutableStateFlow(false)
    val isExistingEntry: StateFlow<Boolean> = _isExistingEntry.asStateFlow()

    init {
        loadEntry(LocalDate.now())
    }

    fun setDate(date: LocalDate) {
        _selectedDate.value = date
        loadEntry(date)
    }

    fun updateDate(newDate: LocalDate) {
        _selectedDate.value = newDate
        _entry.value = _entry.value?.copy(date = newDate)
        // Note: We don't call saveEntry() here to avoid immediate move/delete
        // until the user interacts with another field or clicks Confirm.
    }

    private fun loadEntry(date: LocalDate) {
        viewModelScope.launch {
            val dbEntry = repository.getEntryForDate(date)
            originalDate = if (dbEntry != null) date else null
            _isExistingEntry.value = dbEntry != null

            val entry = dbEntry ?: DailyEntry(
                date = date,
                basalBodyTemperature = null,
                mucusConsistency = null,
                mucusSensation = null
            )
            _entry.value = entry
        }
    }

    fun updateTemperature(temp: Double?) {
        _entry.value = _entry.value?.copy(basalBodyTemperature = temp)
        saveEntry()
    }

    fun updateMucusConsistency(consistency: MucusConsistency?) {
        _entry.value = _entry.value?.copy(mucusConsistency = consistency)
        saveEntry()
    }

    fun updateMucusSensation(sensation: MucusSensation?) {
        _entry.value = _entry.value?.copy(mucusSensation = sensation)
        saveEntry()
    }

    fun updateDisturbances(disturbances: List<String>) {
        _entry.value = _entry.value?.copy(disturbances = disturbances)
        saveEntry()
    }

    fun updateNotes(notes: String) {
        _entry.value = _entry.value?.copy(notes = notes)
        saveEntry()
    }

    fun updateIsPeriod(isPeriod: Boolean) {
        _entry.value = _entry.value?.copy(isPeriod = isPeriod)
        saveEntry()
    }

    fun updateFlow(flow: FlowLevel?) {
        _entry.value = _entry.value?.copy(flow = flow)
        saveEntry()
    }

    fun updatePain(pain: PainLevel?) {
        _entry.value = _entry.value?.copy(pain = pain)
        saveEntry()
    }

    fun updateCervix(
        position: CervicalPosition? = _entry.value?.cervicalPosition,
        firmness: CervicalFirmness? = _entry.value?.cervicalFirmness,
        opening: CervicalOpening? = _entry.value?.cervicalOpening
    ) {
        _entry.value = _entry.value?.copy(
            cervicalPosition = position,
            cervicalFirmness = firmness,
            cervicalOpening = opening
        )
        saveEntry()
    }

    fun saveEntry() {
        val currentEntry = _entry.value ?: return
        viewModelScope.launch {
            if (originalDate != null && originalDate != currentEntry.date) {
                repository.deleteEntryByDate(originalDate!!)
            }
            repository.upsertEntry(currentEntry)
            originalDate = currentEntry.date
            _isExistingEntry.value = true
        }
    }

    fun deleteEntry(onSuccess: () -> Unit) {
        val dateToDelete = originalDate ?: _entry.value?.date ?: return
        viewModelScope.launch {
            repository.deleteEntryByDate(dateToDelete)
            onSuccess()
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
                val application = checkNotNull(extras[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]) as SytothApplication
                return LoggingViewModel(application.repository) as T
            }
        }
    }
}
