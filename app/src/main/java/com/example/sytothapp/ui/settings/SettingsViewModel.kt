package com.example.sytothapp.ui.settings

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.example.sytothapp.SytothApplication
import com.example.sytothapp.data.repository.CycleRepository
import com.example.sytothapp.data.repository.SettingsRepository
import com.example.sytothapp.data.repository.TemperatureUnit
import com.example.sytothapp.data.repository.ThemeMode
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File

class SettingsViewModel(
    private val repository: SettingsRepository,
    private val cycleRepository: CycleRepository
) : ViewModel() {

    val temperatureUnit: StateFlow<TemperatureUnit> = repository.temperatureUnit
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), TemperatureUnit.CELSIUS)

    val themeMode: StateFlow<ThemeMode> = repository.themeMode
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ThemeMode.SYSTEM)

    private val _showSeedSuccess = MutableSharedFlow<Unit>()
    val showSeedSuccess = _showSeedSuccess.asSharedFlow()

    fun seedSampleData() {
        viewModelScope.launch {
            cycleRepository.seedSampleData()
            _showSeedSuccess.emit(Unit)
        }
    }

    fun setTemperatureUnit(unit: TemperatureUnit) {
        viewModelScope.launch {
            repository.setTemperatureUnit(unit)
        }
    }

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch {
            repository.setThemeMode(mode)
        }
    }

    fun exportData(context: Context) {
        viewModelScope.launch {
            val entries = cycleRepository.getAllEntries().first()
            val csvContent = buildString {
                append("Date,Is Period,BBT,Flow,Pain,Mucus Consistency,Mucus Sensation,Disturbances,Notes\n")
                entries.forEach { entry ->
                    append("${entry.date},")
                    append("${entry.isPeriod},")
                    append("${entry.basalBodyTemperature ?: ""},")
                    append("${entry.flow},")
                    append("${entry.pain},")
                    append("${entry.mucusConsistency},")
                    append("${entry.mucusSensation},")
                    append("${entry.disturbances.joinToString("|")},")
                    append("${entry.notes.replace("\n", " ")}\n")
                }
            }

            try {
                val exportDir = File(context.cacheDir, "exports")
                if (!exportDir.exists()) exportDir.mkdirs()
                
                val file = File(exportDir, "sytoth_data_export_${System.currentTimeMillis()}.csv")
                file.writeText(csvContent)

                val contentUri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    file
                )

                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/csv"
                    putExtra(Intent.EXTRA_STREAM, contentUri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                
                context.startActivity(Intent.createChooser(intent, "Export Data"))
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
                val application = checkNotNull(extras[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]) as SytothApplication
                return SettingsViewModel(application.settingsRepository, application.repository) as T
            }
        }
    }
}
