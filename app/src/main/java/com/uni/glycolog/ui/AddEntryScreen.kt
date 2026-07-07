package com.uni.glycolog.ui

import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

@Composable
fun AddEntryScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var bloodSugarLevel by remember { mutableStateOf("") }
    var fastInsulin by remember { mutableStateOf("") }
    var carbohydrates by remember { mutableStateOf("") }
    var physicalActivity by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Button(
            onClick = { onNavigateBack() },
            modifier = Modifier.fillMaxWidth().height(56.dp)
        ) {
            Text("INDIETRO")
        }

        Text("Nuova Misurazione", style = MaterialTheme.typography.headlineMedium)

        OutlinedTextField(
            value = bloodSugarLevel,
            onValueChange = { bloodSugarLevel = it },
            label = { Text("Glicemia (mg/dL)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = fastInsulin,
            onValueChange = { fastInsulin = it },
            label = { Text("Insulina Rapida (UI)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = carbohydrates,
            onValueChange = { carbohydrates = it },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Carboidrati (g)") },
        )

        OutlinedTextField(
            value = notes,
            onValueChange = { notes = it },
            label = { Text("Note") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = {  },
            modifier = Modifier.fillMaxWidth().height(56.dp)
        ) {
            Text("SALVA MISURAZIONE")
        }
    }
}
