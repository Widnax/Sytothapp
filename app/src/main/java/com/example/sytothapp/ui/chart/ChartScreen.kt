package com.example.sytothapp.ui.chart

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sytothapp.data.local.entity.DailyEntry
import com.example.sytothapp.data.local.entity.MucusConsistency
import com.example.sytothapp.data.local.entity.MucusSensation
import com.example.sytothapp.domain.model.FertilityEvaluation
import com.example.sytothapp.domain.model.FertilityStatus
import com.example.sytothapp.ui.theme.SytothappTheme
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChartScreen(
    viewModel: ChartViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Chart") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "Navigate Back"
                        )
                    }
                }
            )
        },
        modifier = modifier.fillMaxSize()
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            if (uiState.entries.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
                    Text("No data available for charting.")
                }
            } else {
                CycleChart(
                    entries = uiState.entries,
                    evaluation = uiState.evaluation,
                    dailyStatuses = uiState.dailyStatuses,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(400.dp)
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                FertilitySummary(evaluation = uiState.evaluation)
            }
        }
    }
}

@Composable
fun CycleChart(
    entries: List<DailyEntry>,
    evaluation: FertilityEvaluation,
    dailyStatuses: List<com.example.sytothapp.domain.model.FertilityStatus>,
    modifier: Modifier = Modifier
) {
    val textMeasurer = rememberTextMeasurer()
    val theme = MaterialTheme.colorScheme
    
    val tempEntries = entries.filter { it.basalBodyTemperature != null }
    if (tempEntries.isEmpty()) return

    val minTemp = (tempEntries.minOf { it.basalBodyTemperature!! } - 0.2).coerceAtMost(36.0)
    val maxTemp = (tempEntries.maxOf { it.basalBodyTemperature!! } + 0.2).coerceAtLeast(37.5)
    val tempRange = maxTemp - minTemp

    Canvas(modifier = modifier.background(theme.surfaceVariant.copy(alpha = 0.3f))) {
        val width = size.width
        val height = size.height
        val padding = 40.dp.toPx()
        val chartWidth = width - padding * 2
        val chartHeight = height - padding * 2

        val stepX = chartWidth / (entries.size.coerceAtLeast(1))
        
        // Draw Fertile Window Shading
        dailyStatuses.forEachIndexed { index, status ->
            if (status == FertilityStatus.FERTILE) {
                val x = padding + index * stepX
                drawRect(
                    color = theme.primary.copy(alpha = 0.1f),
                    topLeft = Offset(x, padding),
                    size = Size(stepX, chartHeight)
                )
            }
        }

        // Draw Y-axis (Temperature)
        val tempStep = 0.1
        var currentTemp = minTemp
        while (currentTemp <= maxTemp) {
            val y = (chartHeight - ((currentTemp - minTemp) / tempRange * chartHeight) + padding).toFloat()
            drawLine(
                color = theme.onSurfaceVariant.copy(alpha = 0.2f),
                start = Offset(padding, y),
                end = Offset(width - padding, y),
                strokeWidth = 1.dp.toPx()
            )
            drawText(
                textMeasurer = textMeasurer,
                text = String.format(Locale.getDefault(), "%.1f", currentTemp),
                topLeft = Offset(5.dp.toPx(), y - 10.sp.toPx()),
                style = TextStyle(color = theme.onSurfaceVariant, fontSize = 10.sp)
            )
            currentTemp += tempStep
        }

        // Draw X-axis (Days)
        entries.indices.forEach { index ->
            val x = padding + index * stepX
            drawText(
                textMeasurer = textMeasurer,
                text = (index + 1).toString(),
                topLeft = Offset(x, chartHeight + padding + 5.dp.toPx()),
                style = TextStyle(color = theme.onSurfaceVariant, fontSize = 10.sp)
            )
        }

        // Draw Cover Line
        evaluation.coverLine?.let { coverLine ->
            val y = (chartHeight - ((coverLine - minTemp) / tempRange * chartHeight) + padding).toFloat()
            drawLine(
                color = theme.error,
                start = Offset(padding, y),
                end = Offset(width - padding, y),
                strokeWidth = 2.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
            )
        }

        // Draw Temperature Line and Dots
        val path = Path()
        var firstPoint = true
        entries.forEachIndexed { index, entry ->
            entry.basalBodyTemperature?.let { temp ->
                val x = padding + index * stepX + stepX / 2
                val y = (chartHeight - ((temp - minTemp) / tempRange * chartHeight) + padding).toFloat()
                if (firstPoint) {
                    path.moveTo(x, y)
                    firstPoint = false
                } else {
                    path.lineTo(x, y)
                }
                
                // Determine if this is one of the 3 high temps
                val shiftDate = evaluation.temperatureShiftDate
                val isHighTemp = shiftDate != null && 
                    (entry.date == shiftDate || entry.date == shiftDate.plusDays(1) || entry.date == shiftDate.plusDays(2))

                drawCircle(
                    color = if (isHighTemp) theme.error else theme.primary,
                    radius = if (isHighTemp) 6.dp.toPx() else 4.dp.toPx(),
                    center = Offset(x, y)
                )
                
                if (isHighTemp) {
                    val dayNum = when(entry.date) {
                        shiftDate -> "1"
                        shiftDate.plusDays(1) -> "2"
                        else -> "3"
                    }
                    drawText(
                        textMeasurer = textMeasurer,
                        text = dayNum,
                        topLeft = Offset(x - 3.dp.toPx(), y - 18.dp.toPx()),
                        style = TextStyle(color = theme.error, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    )
                }
            }
        }
        drawPath(
            path = path,
            color = theme.primary,
            style = Stroke(width = 2.dp.toPx())
        )
        
        // Draw Mucus Markers at the bottom
        entries.forEachIndexed { index, entry ->
            val x = padding + index * stepX + stepX / 2
            val y = chartHeight + padding - 15.dp.toPx()
            
            val mucusLabel = getMucusLabel(entry.mucusConsistency, entry.mucusSensation)
            if (mucusLabel.isNotEmpty()) {
                drawText(
                    textMeasurer = textMeasurer,
                    text = mucusLabel,
                    topLeft = Offset(x - 5.dp.toPx(), y),
                    style = TextStyle(color = theme.secondary, fontSize = 12.sp)
                )
            }
            
            if (entry.date == evaluation.peakDay) {
                drawText(
                    textMeasurer = textMeasurer,
                    text = "P",
                    topLeft = Offset(x - 5.dp.toPx(), y - 15.dp.toPx()),
                    style = TextStyle(color = Color.Red, fontSize = 14.sp)
                )
            }
        }
    }
}

private fun getMucusLabel(consistency: MucusConsistency?, sensation: MucusSensation?): String {
    return when {
        consistency == MucusConsistency.EGG_WHITE || sensation == MucusSensation.SLIPPERY -> "EW"
        consistency == MucusConsistency.CREAMY -> "C"
        consistency == MucusConsistency.STICKY -> "S"
        consistency == MucusConsistency.DRY && sensation == MucusSensation.DRY -> "D"
        else -> ""
    }
}

@Composable
fun FertilitySummary(evaluation: FertilityEvaluation) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                Icon(Icons.Rounded.Info, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Fertility Status", style = MaterialTheme.typography.titleMedium)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = evaluation.status.name,
                style = MaterialTheme.typography.headlineSmall,
                color = if (evaluation.status == FertilityStatus.FERTILE) 
                    MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text("Temperature Shift: ${if (evaluation.isConfirmedTemperatureShift) "Confirmed ✅" else "Not detected ❌"}")
            Text("Mucus Peak: ${if (evaluation.isConfirmedMucusPeak) "Confirmed ✅" else "Not detected ❌"}")
            evaluation.peakDay?.let {
                Text("Peak Day: ${it.format(DateTimeFormatter.ofPattern("MMM dd"))}")
            }
        }
    }
}

@Preview(showBackground = true, device = "spec:width=411dp,height=891dp")
@Composable
fun CycleChartPreview() {
    val entries = listOf(
        DailyEntry(LocalDate.now().minusDays(10), 36.4, null, null),
        DailyEntry(LocalDate.now().minusDays(9), 36.5, null, null),
        DailyEntry(LocalDate.now().minusDays(8), 36.3, MucusConsistency.STICKY, MucusSensation.MOIST),
        DailyEntry(LocalDate.now().minusDays(7), 36.4, MucusConsistency.CREAMY, MucusSensation.WET),
        DailyEntry(LocalDate.now().minusDays(6), 36.2, MucusConsistency.EGG_WHITE, MucusSensation.SLIPPERY),
        DailyEntry(LocalDate.now().minusDays(5), 36.6, MucusConsistency.EGG_WHITE, MucusSensation.SLIPPERY),
        DailyEntry(LocalDate.now().minusDays(4), 36.8, MucusConsistency.STICKY, MucusSensation.DRY),
        DailyEntry(LocalDate.now().minusDays(3), 36.9, null, MucusSensation.DRY),
        DailyEntry(LocalDate.now().minusDays(2), 37.1, null, MucusSensation.DRY),
        DailyEntry(LocalDate.now().minusDays(1), 37.0, null, null)
    )
    
    val evaluation = FertilityEvaluation(
        status = FertilityStatus.INFERTILE,
        isConfirmedTemperatureShift = true,
        isConfirmedMucusPeak = true,
        coverLine = 36.5,
        temperatureShiftDate = LocalDate.now().minusDays(4),
        peakDay = LocalDate.now().minusDays(5)
    )
    
    val dailyStatuses = List(6) { FertilityStatus.FERTILE } + List(4) { FertilityStatus.INFERTILE }

    SytothappTheme {
        Surface {
            Column(modifier = Modifier.padding(16.dp)) {
                CycleChart(
                    entries = entries,
                    evaluation = evaluation,
                    dailyStatuses = dailyStatuses,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(400.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                FertilitySummary(evaluation = evaluation)
            }
        }
    }
}
