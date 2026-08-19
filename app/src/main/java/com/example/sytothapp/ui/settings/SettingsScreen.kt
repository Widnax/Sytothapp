package com.example.sytothapp.ui.settings

import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.sytothapp.R
import com.example.sytothapp.data.repository.TemperatureUnit
import com.example.sytothapp.data.repository.ThemeMode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val temperatureUnit by viewModel.temperatureUnit.collectAsStateWithLifecycle()
    val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(viewModel.showSeedSuccess) {
        viewModel.showSeedSuccess.collect {
            android.widget.Toast.makeText(
                context,
                context.getString(R.string.sample_data_seeded),
                android.widget.Toast.LENGTH_SHORT
            ).show()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                }
            )
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            LanguageSection()
            
            TemperatureSection(
                selectedUnit = temperatureUnit,
                onUnitSelected = viewModel::setTemperatureUnit
            )
            
            ThemeSection(
                selectedMode = themeMode,
                onModeSelected = viewModel::setThemeMode
            )
            
            ExportSection(onExportClick = { viewModel.exportData(context) })

            DebugSection(onSeedClick = viewModel::seedSampleData)
        }
    }
}

@Composable
fun LanguageSection() {
    Column {
        Text(
            text = stringResource(R.string.language),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(8.dp))
        
        val currentLocale = AppCompatDelegate.getApplicationLocales().get(0)?.language ?: "en"
        
        Row(verticalAlignment = Alignment.CenterVertically) {
            RadioButton(
                selected = currentLocale == "en",
                onClick = {
                    val appLocale: LocaleListCompat = LocaleListCompat.forLanguageTags("en")
                    AppCompatDelegate.setApplicationLocales(appLocale)
                }
            )
            Text(
                text = "English",
                modifier = Modifier.clickable {
                    val appLocale: LocaleListCompat = LocaleListCompat.forLanguageTags("en")
                    AppCompatDelegate.setApplicationLocales(appLocale)
                }
            )
        }
        
        Row(verticalAlignment = Alignment.CenterVertically) {
            RadioButton(
                selected = currentLocale == "fr",
                onClick = {
                    val appLocale: LocaleListCompat = LocaleListCompat.forLanguageTags("fr")
                    AppCompatDelegate.setApplicationLocales(appLocale)
                }
            )
            Text(
                text = "Français",
                modifier = Modifier.clickable {
                    val appLocale: LocaleListCompat = LocaleListCompat.forLanguageTags("fr")
                    AppCompatDelegate.setApplicationLocales(appLocale)
                }
            )
        }
    }
}

@Composable
fun TemperatureSection(
    selectedUnit: TemperatureUnit,
    onUnitSelected: (TemperatureUnit) -> Unit
) {
    Column {
        Text(
            text = stringResource(R.string.temperature_unit),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(8.dp))
        
        Row(verticalAlignment = Alignment.CenterVertically) {
            RadioButton(
                selected = selectedUnit == TemperatureUnit.CELSIUS,
                onClick = { onUnitSelected(TemperatureUnit.CELSIUS) }
            )
            Text(
                text = stringResource(R.string.celsius),
                modifier = Modifier.clickable { onUnitSelected(TemperatureUnit.CELSIUS) }
            )
        }
        
        Row(verticalAlignment = Alignment.CenterVertically) {
            RadioButton(
                selected = selectedUnit == TemperatureUnit.FAHRENHEIT,
                onClick = { onUnitSelected(TemperatureUnit.FAHRENHEIT) }
            )
            Text(
                text = stringResource(R.string.fahrenheit),
                modifier = Modifier.clickable { onUnitSelected(TemperatureUnit.FAHRENHEIT) }
            )
        }
    }
}

@Composable
fun ThemeSection(
    selectedMode: ThemeMode,
    onModeSelected: (ThemeMode) -> Unit
) {
    Column {
        Text(
            text = stringResource(R.string.theme),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(8.dp))
        
        ThemeMode.entries.forEach { mode ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(
                    selected = selectedMode == mode,
                    onClick = { onModeSelected(mode) }
                )
                Text(
                    text = when (mode) {
                        ThemeMode.SYSTEM -> stringResource(R.string.theme_system)
                        ThemeMode.LIGHT -> stringResource(R.string.theme_light)
                        ThemeMode.DARK -> stringResource(R.string.theme_dark)
                    },
                    modifier = Modifier.clickable { onModeSelected(mode) }
                )
            }
        }
    }
}

@Composable
fun ExportSection(onExportClick: () -> Unit) {
    Column {
        Button(
            onClick = onExportClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.export_data))
        }
    }
}

@Composable
fun DebugSection(onSeedClick: () -> Unit) {
    Column {
        Text(
            text = stringResource(R.string.debug_section),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(8.dp))
        Button(
            onClick = onSeedClick,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)
        ) {
            Text(stringResource(R.string.seed_sample_data))
        }
    }
}
