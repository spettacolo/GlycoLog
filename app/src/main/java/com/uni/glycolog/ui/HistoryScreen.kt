package com.uni.glycolog.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.uni.glycolog.R
import com.uni.glycolog.data.MeasurementEntity
import com.uni.glycolog.ui.theme.glucoseColor
import com.uni.glycolog.util.Formatters

@Composable
fun HistoryScreen(
    viewModel: MeasurementViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val measurements by viewModel.filteredMeasurements.collectAsStateWithLifecycle()
    val dateFilter by viewModel.dateFilter.collectAsStateWithLifecycle()
    val rangeFilter by viewModel.rangeFilter.collectAsStateWithLifecycle()

    var toDelete by remember { mutableStateOf<MeasurementEntity?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onNavigateBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.back)
                )
            }
            Text(
                text = stringResource(R.string.title_history),
                style = MaterialTheme.typography.headlineSmall
            )
        }

        Row(
            modifier = Modifier
                .horizontalScroll(rememberScrollState())
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = dateFilter == DateFilter.ALL,
                onClick = { viewModel.setDateFilter(DateFilter.ALL) },
                label = { Text(stringResource(R.string.filter_all)) }
            )
            FilterChip(
                selected = dateFilter == DateFilter.LAST_7,
                onClick = { viewModel.setDateFilter(DateFilter.LAST_7) },
                label = { Text(stringResource(R.string.filter_7d)) }
            )
            FilterChip(
                selected = dateFilter == DateFilter.LAST_30,
                onClick = { viewModel.setDateFilter(DateFilter.LAST_30) },
                label = { Text(stringResource(R.string.filter_30d)) }
            )
        }

        Row(
            modifier = Modifier
                .horizontalScroll(rememberScrollState())
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = rangeFilter == RangeFilter.ALL,
                onClick = { viewModel.setRangeFilter(RangeFilter.ALL) },
                label = { Text(stringResource(R.string.filter_all)) }
            )
            FilterChip(
                selected = rangeFilter == RangeFilter.LOW,
                onClick = { viewModel.setRangeFilter(RangeFilter.LOW) },
                label = { Text(stringResource(R.string.filter_low)) }
            )
            FilterChip(
                selected = rangeFilter == RangeFilter.IN_RANGE,
                onClick = { viewModel.setRangeFilter(RangeFilter.IN_RANGE) },
                label = { Text(stringResource(R.string.filter_in_range)) }
            )
            FilterChip(
                selected = rangeFilter == RangeFilter.HIGH,
                onClick = { viewModel.setRangeFilter(RangeFilter.HIGH) },
                label = { Text(stringResource(R.string.filter_high)) }
            )
        }

        if (measurements.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.history_empty),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(measurements, key = { it.id }) { measurement ->
                    MeasurementCard(
                        measurement = measurement,
                        onDelete = { toDelete = measurement }
                    )
                }
            }
        }
    }

    toDelete?.let { measurement ->
        AlertDialog(
            onDismissRequest = { toDelete = null },
            title = { Text(stringResource(R.string.delete_title)) },
            text = { Text(stringResource(R.string.delete_message)) },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteMeasurement(measurement)
                    toDelete = null
                }) {
                    Text(stringResource(R.string.delete))
                }
            },
            dismissButton = {
                TextButton(onClick = { toDelete = null }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}

@Composable
private fun MeasurementCard(
    measurement: MeasurementEntity,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .background(glucoseColor(measurement.bloodSugarLevel), CircleShape)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = stringResource(R.string.value_mg_dl, measurement.bloodSugarLevel),
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    measurement.temporalContext?.let {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = it,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Text(
                    text = Formatters.formatDateTime(measurement.timestamp),
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                val insulinFast = measurement.fastInsulin?.let {
                    stringResource(R.string.ins_rapid_format, Formatters.formatDose(it))
                }
                val insulinSlow = measurement.slowInsulin?.let {
                    stringResource(R.string.ins_slow_format, Formatters.formatDose(it))
                }
                val carbs = measurement.carbohydrates?.let {
                    stringResource(R.string.carbs_format, it)
                }
                val activity = measurement.physicalActivity?.let {
                    stringResource(R.string.activity_format, it)
                }
                val details = listOfNotNull(insulinFast, insulinSlow, carbs, activity)
                if (details.isNotEmpty()) {
                    Text(
                        text = details.joinToString(" · "),
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                measurement.notes?.let {
                    Text(
                        text = it,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = stringResource(R.string.delete),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
