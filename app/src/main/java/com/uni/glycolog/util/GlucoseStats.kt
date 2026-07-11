package com.uni.glycolog.util

import com.uni.glycolog.data.MeasurementEntity
import java.util.Calendar
import kotlin.math.roundToInt
import kotlin.math.sqrt

// classificazione clinica di un valore glicemico
enum class GlucoseLevel { LOW, IN_RANGE, WARNING, HIGH }

// fasce orarie per la distribuzione nel report
enum class TimeSlot(val startHour: Int, val endHour: Int) {
    NIGHT(0, 6),
    MORNING(6, 12),
    AFTERNOON(12, 18),
    EVENING(18, 24)
}

data class TimeSlotStat(val slot: TimeSlot, val count: Int, val mean: Double)

// funzioni pure per le statistiche cliniche (media, TIR, deviazione standard, fasce orarie)
object GlucoseStats {

    // Tempo in Range: percentuale di valori tra 70 e 180 mg/dL (standard ADA)
    const val RANGE_MIN = 70
    const val RANGE_MAX = 180

    // sopra questa soglia il valore e' "attenzione" (giallo), come da specifica di progetto
    const val WARNING_MIN = 140

    // limiti fisici accettati in input (validazione clinica dei glucometri)
    const val INPUT_MIN = 20
    const val INPUT_MAX = 600
    const val INSULIN_MAX = 100.0
    const val CARBS_MAX = 300

    fun classify(value: Int): GlucoseLevel = when {
        value < RANGE_MIN -> GlucoseLevel.LOW
        value < WARNING_MIN -> GlucoseLevel.IN_RANGE
        value <= RANGE_MAX -> GlucoseLevel.WARNING
        else -> GlucoseLevel.HIGH
    }

    fun mean(values: List<Int>): Double = if (values.isEmpty()) 0.0 else values.average()

    // percentuale (arrotondata) di valori dentro il range 70-180
    fun timeInRange(values: List<Int>): Int =
        if (values.isEmpty()) 0
        else (values.count { it in RANGE_MIN..RANGE_MAX } * 100.0 / values.size).roundToInt()

    // deviazione standard campionaria della glicemia
    fun standardDeviation(values: List<Int>): Double {
        if (values.size < 2) return 0.0
        val m = values.average()
        return sqrt(values.sumOf { (it - m) * (it - m) } / (values.size - 1))
    }

    // numero di misurazioni e media per ciascuna fascia oraria
    fun hourlyDistribution(measurements: List<MeasurementEntity>): List<TimeSlotStat> {
        val calendar = Calendar.getInstance()
        return TimeSlot.entries.map { slot ->
            val values = measurements.filter {
                calendar.timeInMillis = it.timestamp
                calendar.get(Calendar.HOUR_OF_DAY) in slot.startHour until slot.endHour
            }.map { it.bloodSugarLevel }
            TimeSlotStat(slot, values.size, mean(values))
        }
    }
}
