package com.uni.glycolog.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface MeasurementDao {

    @Query("SELECT * FROM measurements_table ORDER BY timestamp DESC")
    fun getAll(): Flow<List<MeasurementEntity>>

    @Query("SELECT * FROM measurements_table WHERE timestamp BETWEEN :from AND :to ORDER BY timestamp ASC")
    suspend fun getBetween(from: Long, to: Long): List<MeasurementEntity>

    @Insert
    suspend fun insert(measurement: MeasurementEntity)

    @Delete
    suspend fun delete(measurement: MeasurementEntity)
}
