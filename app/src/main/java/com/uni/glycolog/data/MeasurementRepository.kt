package com.uni.glycolog.data

import kotlinx.coroutines.flow.Flow

// livello intermedio tra ViewModel e DAO (architettura MVVM vista a lezione)
class MeasurementRepository(private val dao: MeasurementDao) {

    val allMeasurements: Flow<List<MeasurementEntity>> = dao.getAll()

    suspend fun getBetween(from: Long, to: Long): List<MeasurementEntity> = dao.getBetween(from, to)

    suspend fun insert(measurement: MeasurementEntity) = dao.insert(measurement)

    suspend fun delete(measurement: MeasurementEntity) = dao.delete(measurement)
}
