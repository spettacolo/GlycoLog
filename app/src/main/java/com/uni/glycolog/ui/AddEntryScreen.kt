package com.uni.glycolog.ui

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.uni.glycolog.R
import com.uni.glycolog.util.NotificationHelper

@Composable
fun AddEntryScreen(
    viewModel: MeasurementViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var glucose by rememberSaveable { mutableStateOf("") }
    var fastInsulin by rememberSaveable { mutableStateOf("") }
    var slowInsulin by rememberSaveable { mutableStateOf("") }
    var carbs by rememberSaveable { mutableStateOf("") }
    var temporalContext by rememberSaveable { mutableStateOf("") }
    var physicalActivity by rememberSaveable { mutableStateOf("") }
    var notes by rememberSaveable { mutableStateOf("") }
    var glucoseError by rememberSaveable { mutableStateOf(false) }

    var saving by remember { mutableStateOf(false) }

    val context = LocalContext.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onNavigateBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.back)
                )
            }
            Text(
                text = stringResource(R.string.title_new_measurement),
                style = MaterialTheme.typography.headlineSmall
            )
        }

        OutlinedTextField(
            value = glucose,
            onValueChange = {
                glucose = it
                glucoseError = false
            },
            label = { Text(stringResource(R.string.hint_glucose)) },
            isError = glucoseError,
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        ContextDropdown(
            selected = temporalContext,
            onSelected = { temporalContext = it }
        )

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedTextField(
                value = fastInsulin,
                onValueChange = { fastInsulin = it },
                label = { Text(stringResource(R.string.hint_fast_insulin)) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = slowInsulin,
                onValueChange = { slowInsulin = it },
                label = { Text(stringResource(R.string.hint_slow_insulin)) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.weight(1f)
            )
        }

        OutlinedTextField(
            value = carbs,
            onValueChange = { carbs = it },
            label = { Text(stringResource(R.string.hint_carbs)) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = physicalActivity,
            onValueChange = { physicalActivity = it },
            label = { Text(stringResource(R.string.hint_activity)) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = notes,
            onValueChange = { notes = it },
            label = { Text(stringResource(R.string.hint_notes)) },
            minLines = 2,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = {
                val errorRes = viewModel.saveMeasurement(
                    glucoseText = glucose,
                    fastInsulinText = fastInsulin,
                    slowInsulinText = slowInsulin,
                    carbsText = carbs,
                    temporalContext = temporalContext,
                    activityText = physicalActivity,
                    notesText = notes
                ) { outOfRangeValue ->
                    NotificationHelper.showOutOfRangeAlert(context, outOfRangeValue)
                    Toast.makeText(
                        context,
                        context.getString(R.string.alert_value_text, outOfRangeValue),
                        Toast.LENGTH_LONG
                    ).show()
                }

                if (errorRes != null) {
                    glucoseError = errorRes == R.string.error_glucose_required ||
                            errorRes == R.string.error_glucose_range
                    Toast.makeText(context, errorRes, Toast.LENGTH_SHORT).show()
                } else {
                    saving = true
                    Toast.makeText(context, R.string.msg_saved, Toast.LENGTH_SHORT).show()
                    onNavigateBack()
                }
            },
            enabled = !saving,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Text(stringResource(R.string.btn_save))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ContextDropdown(
    selected: String,
    onSelected: (String) -> Unit
) {
    val options = stringArrayResource(R.array.temporal_contexts)
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it }
    ) {
        OutlinedTextField(
            value = selected,
            onValueChange = {},
            readOnly = true,
            label = { Text(stringResource(R.string.hint_context)) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onSelected(option)
                        expanded = false
                    }
                )
            }
        }
    }
}
