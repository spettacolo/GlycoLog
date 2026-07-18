package com.uni.glycolog.ui

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import com.uni.glycolog.R
import com.uni.glycolog.util.Formatters
import com.uni.glycolog.util.PdfReportGenerator
import java.io.File
import java.util.Calendar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private enum class ReportPeriod { TODAY, WEEK, MONTH, CUSTOM }

private const val DAY_MS = 24L * 60 * 60 * 1000

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportScreen(
    viewModel: MeasurementViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedPeriod by rememberSaveable { mutableStateOf(ReportPeriod.WEEK) }
    var customFrom by rememberSaveable { mutableStateOf<Long?>(null) }
    var customTo by rememberSaveable { mutableStateOf<Long?>(null) }
    var showRangePicker by remember { mutableStateOf(false) }
    var generating by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
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
                text = stringResource(R.string.title_report),
                style = MaterialTheme.typography.headlineSmall
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = stringResource(R.string.report_period),
            style = MaterialTheme.typography.titleMedium
        )

        Column(modifier = Modifier.selectableGroup()) {
            PeriodOption(ReportPeriod.TODAY, R.string.period_today, selectedPeriod) {
                selectedPeriod = it
            }
            PeriodOption(ReportPeriod.WEEK, R.string.period_week, selectedPeriod) {
                selectedPeriod = it
            }
            PeriodOption(ReportPeriod.MONTH, R.string.period_month, selectedPeriod) {
                selectedPeriod = it
            }
            PeriodOption(ReportPeriod.CUSTOM, R.string.period_custom, selectedPeriod) {
                selectedPeriod = it
            }
        }

        if (selectedPeriod == ReportPeriod.CUSTOM) {
            OutlinedButton(
                onClick = { showRangePicker = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                val from = customFrom
                val to = customTo
                Text(
                    text = if (from != null && to != null) {
                        "${Formatters.formatDate(from)} – ${Formatters.formatDate(to)}"
                    } else {
                        stringResource(R.string.btn_pick_range)
                    }
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                scope.launch {
                    generating = true
                    val now = System.currentTimeMillis()
                    val (from, to) = when (selectedPeriod) {
                        ReportPeriod.TODAY -> startOfToday() to now
                        ReportPeriod.WEEK -> (now - 7 * DAY_MS) to now
                        ReportPeriod.MONTH -> (now - 30 * DAY_MS) to now
                        ReportPeriod.CUSTOM -> (customFrom ?: 0L) to (customTo ?: now)
                    }
                    val measurements = viewModel.getMeasurementsBetween(from, to)
                    if (measurements.isEmpty()) {
                        Toast.makeText(context, R.string.report_empty, Toast.LENGTH_SHORT).show()
                    } else {
                        val periodLabel =
                            "${Formatters.formatDate(from)} – ${Formatters.formatDate(to)}"
                        val file = withContext(Dispatchers.IO) {
                            PdfReportGenerator(context).generate(measurements, periodLabel)
                        }
                        Toast.makeText(
                            context,
                            context.getString(R.string.report_done, file.name),
                            Toast.LENGTH_LONG
                        ).show()
                        shareReport(context, file)
                    }
                    generating = false
                }
            },
            enabled = !generating &&
                    (selectedPeriod != ReportPeriod.CUSTOM || customFrom != null),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Text(
                text = stringResource(
                    if (generating) R.string.report_generating else R.string.btn_generate
                )
            )
        }
    }

    if (showRangePicker) {
        val pickerState = rememberDateRangePickerState()
        DatePickerDialog(
            onDismissRequest = { showRangePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    val start = pickerState.selectedStartDateMillis
                    val end = pickerState.selectedEndDateMillis
                    if (start != null) {
                        customFrom = start
                        customTo = (end ?: start) + DAY_MS - 1
                    }
                    showRangePicker = false
                }) {
                    Text(stringResource(R.string.ok))
                }
            },
            dismissButton = {
                TextButton(onClick = { showRangePicker = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        ) {
            DateRangePicker(
                state = pickerState,
                showModeToggle = false,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun PeriodOption(
    period: ReportPeriod,
    labelRes: Int,
    selected: ReportPeriod,
    onSelected: (ReportPeriod) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .selectable(
                selected = selected == period,
                onClick = { onSelected(period) },
                role = Role.RadioButton
            )
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(selected = selected == period, onClick = null)
        Spacer(modifier = Modifier.width(8.dp))
        Text(stringResource(labelRes))
    }
}

private fun startOfToday(): Long =
    Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis

private fun shareReport(context: Context, file: File) {
    val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "application/pdf"
        putExtra(Intent.EXTRA_STREAM, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(
        Intent.createChooser(intent, context.getString(R.string.share_report))
    )
}
