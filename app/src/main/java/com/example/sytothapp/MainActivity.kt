package com.example.sytothapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.layout.AnimatedPane
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffold
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffoldRole
import androidx.compose.material3.adaptive.navigation.rememberListDetailPaneScaffoldNavigator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.rememberNavBackStack
import com.example.sytothapp.data.repository.ThemeMode
import com.example.sytothapp.ui.chart.ChartScreen
import com.example.sytothapp.ui.chart.ChartViewModel
import com.example.sytothapp.ui.dashboard.DashboardScreen
import com.example.sytothapp.ui.dashboard.DashboardViewModel
import com.example.sytothapp.ui.logging.LoggingScreen
import com.example.sytothapp.ui.logging.LoggingViewModel
import com.example.sytothapp.ui.navigation.SytothRoute
import com.example.sytothapp.ui.settings.SettingsScreen
import com.example.sytothapp.ui.settings.SettingsViewModel
import com.example.sytothapp.ui.theme.SytothappTheme
import java.time.LocalDate

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val settingsViewModel: SettingsViewModel = viewModel(factory = SettingsViewModel.Factory)
            val themeMode by settingsViewModel.themeMode.collectAsStateWithLifecycle()
            
            SytothappTheme(
                darkTheme = when (themeMode) {
                    ThemeMode.LIGHT -> false
                    ThemeMode.DARK -> true
                    ThemeMode.SYSTEM -> androidx.compose.foundation.isSystemInDarkTheme()
                }
            ) {
                SytothApp()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun SytothApp() {
    // Initialize Navigation 3 Backstack
    val backstack = rememberNavBackStack(SytothRoute.Dashboard)
    
    // Initialize Adaptive Navigator
    val navigator = rememberListDetailPaneScaffoldNavigator<SytothRoute>()
    
    // Sync backstack with adaptive navigator
    LaunchedEffect(backstack.size) {
        val last = backstack.lastOrNull()
        if (last is SytothRoute.Logging || last is SytothRoute.Chart || last is SytothRoute.Settings) {
            navigator.navigateTo(ListDetailPaneScaffoldRole.Detail, last)
        } else {
            navigator.navigateTo(ListDetailPaneScaffoldRole.List)
        }
    }

    ListDetailPaneScaffold(
        directive = navigator.scaffoldDirective,
        value = navigator.scaffoldValue,
        listPane = {
            AnimatedPane {
                val dashboardViewModel: DashboardViewModel = viewModel(factory = DashboardViewModel.Factory)
                DashboardScreen(
                    viewModel = dashboardViewModel,
                    onNavigateToLogging = { date ->
                        val route = SytothRoute.Logging(date)
                        if (!backstack.contains(route)) {
                            backstack.add(route)
                        }
                    },
                    onNavigateToChart = {
                        if (!backstack.contains(SytothRoute.Chart)) {
                            backstack.add(SytothRoute.Chart)
                        }
                    },
                    onNavigateToSettings = {
                        if (!backstack.contains(SytothRoute.Settings)) {
                            backstack.add(SytothRoute.Settings)
                        }
                    }
                )
            }
        },
        detailPane = {
            AnimatedPane {
                // Show detail pane if either it's explicitly navigated to or on large screens
                val selectedRoute = navigator.currentDestination?.contentKey
                when (selectedRoute) {
                    is SytothRoute.Logging -> {
                        val loggingViewModel: LoggingViewModel = viewModel(factory = LoggingViewModel.Factory)
                        LoggingScreen(
                            viewModel = loggingViewModel,
                            initialDate = selectedRoute.date,
                            onNavigateBack = {
                                if (backstack.size > 1) {
                                    backstack.removeAt(backstack.size - 1)
                                }
                            }
                        )
                    }
                    is SytothRoute.Chart -> {
                        val chartViewModel: ChartViewModel = viewModel(factory = ChartViewModel.Factory)
                        ChartScreen(
                            viewModel = chartViewModel,
                            onNavigateBack = {
                                if (backstack.size > 1) {
                                    backstack.removeAt(backstack.size - 1)
                                }
                            }
                        )
                    }
                    is SytothRoute.Settings -> {
                        val settingsViewModel: SettingsViewModel = viewModel(factory = SettingsViewModel.Factory)
                        SettingsScreen(
                            viewModel = settingsViewModel,
                            onNavigateBack = {
                                if (backstack.size > 1) {
                                    backstack.removeAt(backstack.size - 1)
                                }
                            }
                        )
                    }
                    else -> {}
                }
            }
        }
    )
    
    // Handle back button for the navigation stack
    BackHandler(backstack.size > 1) {
        backstack.removeAt(backstack.size - 1)
    }
}
