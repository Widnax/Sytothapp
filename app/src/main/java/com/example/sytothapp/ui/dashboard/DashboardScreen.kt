package com.example.sytothapp.ui.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ShowChart
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.sytothapp.R
import com.example.sytothapp.data.local.entity.DailyEntry
import com.example.sytothapp.data.local.entity.MucusConsistency
import com.example.sytothapp.data.local.entity.MucusSensation
import com.example.sytothapp.data.repository.TemperatureUnit
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    onNavigateToLogging: (LocalDate?) -> Unit,
    onNavigateToChart: () -> Unit,
    onNavigateToSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val entries by viewModel.entries.collectAsStateWithLifecycle(initialValue = emptyList())
    val temperatureUnit by viewModel.temperatureUnit.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.dashboard_title)) },
                actions = {
                    IconButton(onClick = onNavigateToChart) {
                        Icon(Icons.AutoMirrored.Rounded.ShowChart, contentDescription = stringResource(R.string.view_chart))
                    }
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Default.Settings, contentDescription = stringResource(R.string.settings_title))
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { onNavigateToLogging(null) }) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.go_to_daily_log))
            }
        },
        modifier = modifier
    ) { innerPadding ->
        if (entries.isEmpty()) {
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(stringResource(R.string.no_entries))
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = { onNavigateToLogging(null) }) {
                    Text(stringResource(R.string.go_to_daily_log))
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    Text(stringResource(R.string.recent_entries), style = MaterialTheme.typography.headlineSmall)
                }
                items(entries) { entry ->
                    EntryItem(
                        entry = entry,
                        temperatureUnit = temperatureUnit,
                        onClick = { onNavigateToLogging(entry.date) }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EntryItem(
    entry: DailyEntry,
    temperatureUnit: TemperatureUnit,
    onClick: () -> Unit
) {
    val containerColor = when {
        entry.isPeriod -> MaterialTheme.colorScheme.errorContainer
        entry.mucusConsistency == MucusConsistency.EGG_WHITE || entry.mucusSensation == MucusSensation.SLIPPERY -> {
            MaterialTheme.colorScheme.primaryContainer
        }
        entry.mucusConsistency == MucusConsistency.CREAMY || entry.mucusConsistency == MucusConsistency.STICKY -> {
            MaterialTheme.colorScheme.secondaryContainer
        }
        !entry.isPeriod && entry.basalBodyTemperature == null &&
                (entry.mucusConsistency == null || entry.mucusConsistency == MucusConsistency.NONE) &&
                (entry.mucusSensation == null || entry.mucusSensation == MucusSensation.NONE) -> {
            MaterialTheme.colorScheme.tertiaryContainer
        }
        else -> MaterialTheme.colorScheme.surfaceVariant
    }

    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = entry.date.format(DateTimeFormatter.ofPattern("EEEE, MMM dd")),
                style = MaterialTheme.typography.titleMedium
            )
            if (entry.isPeriod) {
                Text(
                    text = stringResource(R.string.label_period),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            } else {
                entry.basalBodyTemperature?.let {
                    val tempText = if (temperatureUnit == TemperatureUnit.FAHRENHEIT) {
                        val fahrenheit = (it * 9 / 5) + 32
                        String.format("%.2f°F", fahrenheit)
                    } else {
                        String.format("%.2f°C", it)
                    }
                    Text(stringResource(R.string.temperature_label, tempText))
                }
            }
            if (entry.mucusConsistency != null || entry.mucusSensation != null) {
                Text(stringResource(R.string.mucus_label, entry.mucusConsistency ?: "-", entry.mucusSensation ?: "-"))
            }
            if (entry.disturbances.isNotEmpty()) {
                Text(stringResource(R.string.disturbances_label, entry.disturbances.joinToString(", ")))
            }
        }
    }
}
