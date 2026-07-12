package com.uni.glycolog.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.uni.glycolog.data.MeasurementRepository

// factory manuale per passare il repository al ViewModel (pattern visto a lezione, niente DI)
class MeasurementViewModelFactory(
    private val repository: MeasurementRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MeasurementViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MeasurementViewModel(repository) as T
        }
        throw IllegalArgumentException("Classe ViewModel sconosciuta: ${modelClass.name}")
    }
}
