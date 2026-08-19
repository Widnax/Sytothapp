package com.example.sytothapp.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import com.example.sytothapp.SytothApplication
import com.example.sytothapp.data.local.entity.DailyEntry
import com.example.sytothapp.data.repository.CycleRepository
import kotlinx.coroutines.flow.Flow

class DashboardViewModel(private val repository: CycleRepository) : ViewModel() {
    val entries: Flow<List<DailyEntry>> = repository.getAllEntries()

    companion object {
        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
                val application = checkNotNull(extras[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]) as SytothApplication
                return DashboardViewModel(application.repository) as T
            }
        }
    }
}
