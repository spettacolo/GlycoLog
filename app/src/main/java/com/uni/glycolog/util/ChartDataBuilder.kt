package com.uni.glycolog.util

import com.uni.glycolog.data.MeasurementEntity
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

enum class ChartPeriod { HOURS_24, DAYS_7, DAYS_30 }

data class ChartPoint(val position: Float, val value: Float)

data class ChartData(val points: List<ChartPoint>, val labels: List<String>)

object ChartDataBuilder {

    private const val DAY_MS = 24L * 60 * 60 * 1000
    private const val HOUR_MS = 60L * 60 * 1000

    fun build(
        measurements: List<MeasurementEntity>,
        period: ChartPeriod,
        now: Long = System.currentTimeMillis()
    ): ChartData = when (period) {
        ChartPeriod.HOURS_24 -> buildHours(measurements, now)
        ChartPeriod.DAYS_7 -> buildDays(measurements, 7, now)
        ChartPeriod.DAYS_30 -> buildDays(measurements, 30, now)
    }

    private fun buildHours(measurements: List<MeasurementEntity>, now: Long): ChartData {
        val calendar = Calendar.getInstance().apply {
            timeInMillis = now
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        var to = calendar.timeInMillis
        if (to < now) to += HOUR_MS
        val from = to - DAY_MS

        val points = measurements
            .filter { it.timestamp in from..now }
            .sortedBy { it.timestamp }
            .map { ChartPoint((it.timestamp - from).toFloat() / DAY_MS, it.bloodSugarLevel.toFloat()) }

        val formatter = SimpleDateFormat("HH:00", Locale.getDefault())
        val labels = (0..4).map { i -> formatter.format(Date(from + i * 6 * HOUR_MS)) }
        return ChartData(points, labels)
    }

    private fun buildDays(measurements: List<MeasurementEntity>, days: Int, now: Long): ChartData {
        val calendar = Calendar.getInstance().apply {
            timeInMillis = now
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            add(Calendar.DAY_OF_YEAR, -(days - 1))
        }

        val dayStarts = LongArray(days + 1)
        for (i in 0..days) {
            dayStarts[i] = calendar.timeInMillis
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }

        fun dayIndexOf(timestamp: Long): Int {
            for (i in 0 until days) {
                if (timestamp >= dayStarts[i] && timestamp < dayStarts[i + 1]) return i
            }
            return -1
        }

        val points = measurements
            .groupBy { dayIndexOf(it.timestamp) }
            .filterKeys { it >= 0 }
            .entries
            .sortedBy { it.key }
            .map { (dayIndex, list) ->
                ChartPoint(
                    position = dayIndex.toFloat() / (days - 1),
                    value = list.map { it.bloodSugarLevel }.average().toFloat()
                )
            }

        val labels = if (days == 7) {
            val dayFormatter = SimpleDateFormat("EEE", Locale.getDefault())
            (0 until days).map { i ->
                if (i == days - 1) "Oggi"
                else dayFormatter.format(Date(dayStarts[i]))
                    .replaceFirstChar { it.uppercase() }
                    .removeSuffix(".")
            }
        } else {
            val dateFormatter = SimpleDateFormat("dd/MM", Locale.getDefault())
            (0..4).map { i ->
                dateFormatter.format(Date(dayStarts[(i * (days - 1)) / 4]))
            }
        }
        return ChartData(points, labels)
    }
}
