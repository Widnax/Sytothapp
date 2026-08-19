package com.example.sytothapp.ui.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ShowChart
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.sytothapp.data.local.entity.DailyEntry
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    onNavigateToLogging: (LocalDate?) -> Unit,
    onNavigateToChart: () -> Unit,
    modifier: Modifier = Modifier
) {
    val entries by viewModel.entries.collectAsStateWithLifecycle(initialValue = emptyList())

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Sytoth Dashboard") },
                actions = {
                    IconButton(onClick = onNavigateToChart) {
                        Icon(Icons.AutoMirrored.Rounded.ShowChart, contentDescription = "View Chart")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { onNavigateToLogging(null) }) {
                Icon(Icons.Default.Add, contentDescription = "Add Entry")
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
                Text("No entries yet. Start logging your cycle!")
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = { onNavigateToLogging(null) }) {
                    Text("Go to Daily Log")
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
                    Text("Recent Entries", style = MaterialTheme.typography.headlineSmall)
                }
                items(entries) { entry ->
                    EntryItem(
                        entry = entry,
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
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = entry.date.format(DateTimeFormatter.ofPattern("EEEE, MMM dd")),
                style = MaterialTheme.typography.titleMedium
            )
            entry.basalBodyTemperature?.let {
                Text("Temperature: $it°C")
            }
            if (entry.mucusConsistency != null || entry.mucusSensation != null) {
                Text("Mucus: ${entry.mucusConsistency ?: "-"} / ${entry.mucusSensation ?: "-"}")
            }
            if (entry.disturbances.isNotEmpty()) {
                Text("Disturbances: ${entry.disturbances.joinToString(", ")}")
            }
        }
    }
}
