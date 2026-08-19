package com.example.sytothapp.ui.logging

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.sytothapp.data.local.entity.*
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoggingScreen(
    viewModel: LoggingViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    initialDate: LocalDate? = null
) {
    val entry by viewModel.entry.collectAsStateWithLifecycle()
    val selectedDate by viewModel.selectedDate.collectAsStateWithLifecycle()
    val isExistingEntry by viewModel.isExistingEntry.collectAsStateWithLifecycle()

    var showDatePicker by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    LaunchedEffect(initialDate) {
        viewModel.setDate(initialDate ?: LocalDate.now())
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Daily Log - ${selectedDate.format(DateTimeFormatter.ofPattern("MMM dd, yyyy"))}") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "Navigate Back"
                        )
                    }
                },
                actions = {
                    if (isExistingEntry) {
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(Icons.Rounded.Delete, contentDescription = "Delete Log")
                        }
                    }
                    IconButton(onClick = { showDatePicker = true }) {
                        Icon(Icons.Default.CalendarToday, contentDescription = "Change Date")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    viewModel.saveEntry()
                    onNavigateBack()
                }
            ) {
                Icon(Icons.Rounded.Check, contentDescription = "Confirm")
            }
        },
        modifier = modifier
    ) { innerPadding ->
        entry?.let { currentEntry ->
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // BBT
                var tempText by remember(currentEntry.basalBodyTemperature) {
                    mutableStateOf(currentEntry.basalBodyTemperature?.toString() ?: "")
                }
                OutlinedTextField(
                    value = tempText,
                    onValueChange = {
                        tempText = it
                        val temp = it.toDoubleOrNull()
                        if (temp != null || it.isEmpty()) {
                            viewModel.updateTemperature(temp)
                        }
                    },
                    label = { Text("Basal Body Temperature (°C)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Period Toggle
                Text("Entry Type", style = MaterialTheme.typography.titleMedium)
                SingleChoiceSegmentedButtonRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    SegmentedButton(
                        shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
                        onClick = { viewModel.updateIsPeriod(false) },
                        selected = !currentEntry.isPeriod
                    ) {
                        Text("Regular")
                    }
                    SegmentedButton(
                        shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
                        onClick = { viewModel.updateIsPeriod(true) },
                        selected = currentEntry.isPeriod
                    ) {
                        Text("Period")
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                if (currentEntry.isPeriod) {
                    // Flow Level
                    Text("Flow Level", style = MaterialTheme.typography.titleMedium)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                    ) {
                        SingleChoiceSegmentedButtonRow(
                            modifier = Modifier.padding(vertical = 8.dp)
                        ) {
                            FlowLevel.entries.forEachIndexed { index, flow ->
                                SegmentedButton(
                                    shape = SegmentedButtonDefaults.itemShape(
                                        index = index,
                                        count = FlowLevel.entries.size
                                    ),
                                    onClick = { viewModel.updateFlow(flow) },
                                    selected = currentEntry.flow == flow
                                ) {
                                    Text(
                                        text = flow.name.lowercase().replaceFirstChar { it.uppercase() },
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Pain Level
                    Text("Pain Level", style = MaterialTheme.typography.titleMedium)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                    ) {
                        SingleChoiceSegmentedButtonRow(
                            modifier = Modifier.padding(vertical = 8.dp)
                        ) {
                            PainLevel.entries.forEachIndexed { index, pain ->
                                SegmentedButton(
                                    shape = SegmentedButtonDefaults.itemShape(
                                        index = index,
                                        count = PainLevel.entries.size
                                    ),
                                    onClick = { viewModel.updatePain(pain) },
                                    selected = currentEntry.pain == pain
                                ) {
                                    Text(
                                        text = pain.name.lowercase().replaceFirstChar { it.uppercase() },
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                }
                            }
                        }
                    }
                } else {
                    // Mucus Consistency
                    Text("Mucus Consistency", style = MaterialTheme.typography.titleMedium)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                    ) {
                        SingleChoiceSegmentedButtonRow(
                            modifier = Modifier.padding(vertical = 8.dp)
                        ) {
                            MucusConsistency.entries.forEachIndexed { index, consistency ->
                                SegmentedButton(
                                    shape = SegmentedButtonDefaults.itemShape(
                                        index = index,
                                        count = MucusConsistency.entries.size
                                    ),
                                    onClick = { viewModel.updateMucusConsistency(consistency) },
                                    selected = currentEntry.mucusConsistency == consistency
                                ) {
                                    Text(
                                        text = when (consistency) {
                                            MucusConsistency.EGG_WHITE -> "Egg white"
                                            MucusConsistency.NONE -> "None"
                                            else -> consistency.name.lowercase()
                                                .replaceFirstChar { it.uppercase() }
                                        },
                                        style = MaterialTheme.typography.labelSmall,
                                        maxLines = 1,
                                        overflow = TextOverflow.Visible,
                                        softWrap = false
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Mucus Sensation
                    Text("Mucus Sensation", style = MaterialTheme.typography.titleMedium)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                    ) {
                        SingleChoiceSegmentedButtonRow(
                            modifier = Modifier.padding(vertical = 8.dp)
                        ) {
                            MucusSensation.entries.forEachIndexed { index, sensation ->
                                SegmentedButton(
                                    shape = SegmentedButtonDefaults.itemShape(
                                        index = index,
                                        count = MucusSensation.entries.size
                                    ),
                                    onClick = { viewModel.updateMucusSensation(sensation) },
                                    selected = currentEntry.mucusSensation == sensation
                                ) {
                                    Text(
                                        text = when (sensation) {
                                            MucusSensation.NONE -> "None"
                                            else -> sensation.name.lowercase()
                                                .replaceFirstChar { it.uppercase() }
                                        },
                                        style = MaterialTheme.typography.labelSmall,
                                        maxLines = 1,
                                        overflow = TextOverflow.Visible,
                                        softWrap = false
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Disturbances
                    Text("Disturbances", style = MaterialTheme.typography.titleMedium)
                    val commonDisturbances = listOf("Poor Sleep", "Alcohol", "Stress", "Illness", "Travel")
                    commonDisturbances.forEach { disturbance ->
                        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = currentEntry.disturbances.contains(disturbance),
                                onCheckedChange = { checked ->
                                    val newList = if (checked) {
                                        currentEntry.disturbances + disturbance
                                    } else {
                                        currentEntry.disturbances - disturbance
                                    }
                                    viewModel.updateDisturbances(newList)
                                }
                            )
                            Text(disturbance, modifier = Modifier.padding(start = 8.dp))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Notes
                OutlinedTextField(
                    value = currentEntry.notes,
                    onValueChange = { viewModel.updateNotes(it) },
                    label = { Text("Notes") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )
            }
        }

        if (showDatePicker) {
            val datePickerState = rememberDatePickerState(
                initialSelectedDateMillis = selectedDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
            )
            DatePickerDialog(
                onDismissRequest = { showDatePicker = false },
                confirmButton = {
                    TextButton(onClick = {
                        datePickerState.selectedDateMillis?.let {
                            val newDate = Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate()
                            viewModel.updateDate(newDate)
                        }
                        showDatePicker = false
                    }) {
                        Text("OK")
                    }
                }
            ) {
                DatePicker(state = datePickerState)
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Log?") },
            text = { Text("Are you sure you want to delete this entry? This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteEntry(onSuccess = onNavigateBack)
                        showDeleteDialog = false
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
