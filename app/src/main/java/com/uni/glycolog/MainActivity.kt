package com.uni.glycolog

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.uni.glycolog.data.AppDatabase
import com.uni.glycolog.data.MeasurementRepository
import com.uni.glycolog.ui.AppNavigation
import com.uni.glycolog.ui.MeasurementViewModel
import com.uni.glycolog.ui.MeasurementViewModelFactory
import com.uni.glycolog.ui.theme.GlycoLogTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val database = AppDatabase.getDatabase(this)
        val repository = MeasurementRepository(database.measurementDao())
        val factory = MeasurementViewModelFactory(repository)

        setContent {
            GlycoLogTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    val viewModel: MeasurementViewModel = viewModel(factory = factory)
                    AppNavigation(
                        viewModel = viewModel,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}
