package com.example.floworbit.presentation.analytics

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.patrykandpatrick.vico.compose.axis.horizontal.bottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.startAxis
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.column.columnChart
import com.patrykandpatrick.vico.core.axis.AxisPosition
import com.patrykandpatrick.vico.core.axis.formatter.AxisValueFormatter
import com.patrykandpatrick.vico.core.entry.ChartEntryModelProducer
import com.patrykandpatrick.vico.core.entry.entryOf
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsScreen(
    onBack: () -> Unit,
    vm: AnalyticsViewModel = viewModel()
) {
    // 1. Collect the violation data from the ViewModel
    val violationData by vm.violationData.collectAsState()

    // 2. Prepare the data for the Vico chart library
    val chartModelProducer = ChartEntryModelProducer(
        // Convert our map of violations (e.g., "Facebook" -> 12) into chart entries
        violationData.entries.mapIndexed { index, entry ->
            entryOf(index.toFloat(), entry.value) // x=0, y=12; x=1, y=8; etc.
        }
    )

    // 3. Create a formatter to show app names at the bottom of the chart
    val bottomAxisValueFormatter = AxisValueFormatter<AxisPosition.Horizontal.Bottom> { value, _ ->
        // For a given x-position (0, 1, 2...), find the corresponding app name
        violationData.keys.elementAtOrNull(value.toInt()) ?: ""
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Usage Analytics") },
                navigationIcon = {
                    // Changed to a regular IconButton for consistency
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        // 4. Check if there is any data. If not, show a message.
        if (violationData.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text("No violation data recorded yet!")
            }
        } else {
            // If there IS data, show the chart.
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "Top Procrastination Apps",
                    style = MaterialTheme.typography.titleLarge
                )
                Spacer(modifier = Modifier.height(24.dp))

                // 5. This is the Vico Chart Composable that draws the infographic
                Chart(
                    chart = columnChart(), // We want a bar chart (column chart)
                    chartModelProducer = chartModelProducer, // The data for the chart
                    startAxis = startAxis(), // The vertical axis (Y-axis) with numbers
                    bottomAxis = bottomAxis(valueFormatter = bottomAxisValueFormatter), // The horizontal axis (X-axis) with app names
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                )
            }
        }
    }
}
