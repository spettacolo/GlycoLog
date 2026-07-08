package com.uni.glycolog.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "measurements_table")
data class MeasurementEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val bloodSugarLevel: Int,
    val fastInsulin: Double? = null,
    val slowInsulin: Double? = null,
    val carbohydrates: Int? = null,
    val temporalContext: String? = null,
    val physicalActivity: String? = null,
    val notes: String? = null
)