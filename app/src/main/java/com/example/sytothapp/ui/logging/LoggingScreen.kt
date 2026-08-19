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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.sytothapp.R
import com.example.sytothapp.data.local.entity.*
import com.example.sytothapp.data.repository.TemperatureUnit
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
    val temperatureUnit by viewModel.temperatureUnit.collectAsStateWithLifecycle()

    var showDatePicker by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    LaunchedEffect(initialDate) {
        viewModel.setDate(initialDate ?: LocalDate.now())
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.daily_log_title, selectedDate.format(DateTimeFormatter.ofPattern("MMM dd, yyyy")))) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = stringResource(R.string.navigate_back)
                        )
                    }
                },
                actions = {
                    if (isExistingEntry) {
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(Icons.Rounded.Delete, contentDescription = stringResource(R.string.delete_log))
                        }
                    }
                    IconButton(onClick = { showDatePicker = true }) {
                        Icon(Icons.Default.CalendarToday, contentDescription = stringResource(R.string.change_date))
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
                Icon(Icons.Rounded.Check, contentDescription = stringResource(R.string.confirm))
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
                // Period Toggle (Moved to top)
                Text(stringResource(R.string.entry_type), style = MaterialTheme.typography.titleMedium)
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
                        Text(stringResource(R.string.regular))
                    }
                    SegmentedButton(
                        shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
                        onClick = { viewModel.updateIsPeriod(true) },
                        selected = currentEntry.isPeriod
                    ) {
                        Text(stringResource(R.string.label_period))
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                if (!currentEntry.isPeriod) {
                    // BBT (Hidden when Period is selected)
                    val displayTemp = currentEntry.basalBodyTemperature?.let {
                        if (temperatureUnit == TemperatureUnit.FAHRENHEIT) {
                            (it * 9 / 5) + 32
                        } else {
                            it
                        }
                    }

                    var tempText by remember(displayTemp) {
                        mutableStateOf(displayTemp?.let { String.format("%.2f", it) } ?: "")
                    }

                    OutlinedTextField(
                        value = tempText,
                        onValueChange = {
                            tempText = it
                            val inputTemp = it.toDoubleOrNull()
                            if (inputTemp != null || it.isEmpty()) {
                                val tempInCelsius =
                                    if (inputTemp != null && temperatureUnit == TemperatureUnit.FAHRENHEIT) {
                                        (inputTemp - 32) * 5 / 9
                                    } else {
                                        inputTemp
                                    }
                                viewModel.updateTemperature(tempInCelsius)
                            }
                        },
                        label = {
                            Text(
                                if (temperatureUnit == TemperatureUnit.FAHRENHEIT)
                                    stringResource(R.string.bbt_f)
                                else
                                    stringResource(R.string.bbt_c)
                            )
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(24.dp))
                }

                if (currentEntry.isPeriod) {
                    // Flow Level
                    Text(stringResource(R.string.flow_level), style = MaterialTheme.typography.titleMedium)
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
                    Text(stringResource(R.string.pain_level), style = MaterialTheme.typography.titleMedium)
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
                    Text(stringResource(R.string.mucus_consistency), style = MaterialTheme.typography.titleMedium)
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
                                            MucusConsistency.EGG_WHITE -> stringResource(R.string.egg_white)
                                            MucusConsistency.NONE -> stringResource(R.string.none)
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
                    Text(stringResource(R.string.mucus_sensation), style = MaterialTheme.typography.titleMedium)
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
                                            MucusSensation.NONE -> stringResource(R.string.none)
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
                    Text(stringResource(R.string.disturbances), style = MaterialTheme.typography.titleMedium)
                    val commonDisturbances = mapOf(
                        "Poor Sleep" to R.string.poor_sleep,
                        "Alcohol" to R.string.alcohol,
                        "Stress" to R.string.stress,
                        "Illness" to R.string.illness,
                        "Travel" to R.string.travel
                    )
                    commonDisturbances.forEach { (key, resId) ->
                        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = currentEntry.disturbances.contains(key),
                                onCheckedChange = { checked ->
                                    val newList = if (checked) {
                                        currentEntry.disturbances + key
                                    } else {
                                        currentEntry.disturbances - key
                                    }
                                    viewModel.updateDisturbances(newList)
                                }
                            )
                            Text(stringResource(resId), modifier = Modifier.padding(start = 8.dp))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Notes
                OutlinedTextField(
                    value = currentEntry.notes,
                    onValueChange = { viewModel.updateNotes(it) },
                    label = { Text(stringResource(R.string.notes)) },
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
                        Text(stringResource(R.string.ok))
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
            title = { Text(stringResource(R.string.delete_dialog_title)) },
            text = { Text(stringResource(R.string.delete_dialog_text)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteEntry(onSuccess = onNavigateBack)
                        showDeleteDialog = false
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text(stringResource(R.string.delete))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}
