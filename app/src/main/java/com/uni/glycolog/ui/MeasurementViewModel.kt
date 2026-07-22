package com.uni.glycolog.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.uni.glycolog.R
import com.uni.glycolog.data.MeasurementEntity
import com.uni.glycolog.data.MeasurementRepository
import com.uni.glycolog.util.ChartData
import com.uni.glycolog.util.ChartDataBuilder
import com.uni.glycolog.util.ChartPeriod
import com.uni.glycolog.util.GlucoseStats
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

enum class DateFilter { ALL, LAST_7, LAST_30 }
enum class RangeFilter { ALL, LOW, IN_RANGE, HIGH }

data class WeeklyStats(val mean: Int, val timeInRange: Int, val count: Int)

class MeasurementViewModel(private val repository: MeasurementRepository) : ViewModel() {

    companion object {
        private const val WEEK_MS = 7L * 24 * 60 * 60 * 1000
        private const val MONTH_MS = 30L * 24 * 60 * 60 * 1000
    }

    val measurements: StateFlow<List<MeasurementEntity>> = repository.allMeasurements
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val lastMeasurement: StateFlow<MeasurementEntity?> = measurements
        .map { it.firstOrNull() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _chartPeriod = MutableStateFlow(ChartPeriod.DAYS_7)
    val chartPeriod: StateFlow<ChartPeriod> = _chartPeriod.asStateFlow()

    fun setChartPeriod(period: ChartPeriod) {
        _chartPeriod.value = period
    }

    val chartData: StateFlow<ChartData> = combine(measurements, _chartPeriod) { list, period ->
        ChartDataBuilder.build(list, period)
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        ChartData(emptyList(), emptyList())
    )

    val weeklyStats: StateFlow<WeeklyStats> = measurements.map { list ->
        val values = list
            .filter { it.timestamp >= System.currentTimeMillis() - WEEK_MS }
            .map { it.bloodSugarLevel }
        WeeklyStats(
            mean = GlucoseStats.mean(values).roundToInt(),
            timeInRange = GlucoseStats.timeInRange(values),
            count = values.size
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), WeeklyStats(0, 0, 0))

    private val _dateFilter = MutableStateFlow(DateFilter.ALL)
    val dateFilter: StateFlow<DateFilter> = _dateFilter.asStateFlow()

    private val _rangeFilter = MutableStateFlow(RangeFilter.ALL)
    val rangeFilter: StateFlow<RangeFilter> = _rangeFilter.asStateFlow()

    fun setDateFilter(filter: DateFilter) {
        _dateFilter.value = filter
    }

    fun setRangeFilter(filter: RangeFilter) {
        _rangeFilter.value = filter
    }

    val filteredMeasurements: StateFlow<List<MeasurementEntity>> =
        combine(measurements, _dateFilter, _rangeFilter) { list, dateFilter, rangeFilter ->
            val fromTime = when (dateFilter) {
                DateFilter.ALL -> 0L
                DateFilter.LAST_7 -> System.currentTimeMillis() - WEEK_MS
                DateFilter.LAST_30 -> System.currentTimeMillis() - MONTH_MS
            }
            list.filter { it.timestamp >= fromTime }
                .filter {
                    when (rangeFilter) {
                        RangeFilter.ALL -> true
                        RangeFilter.LOW -> it.bloodSugarLevel < GlucoseStats.RANGE_MIN
                        RangeFilter.IN_RANGE ->
                            it.bloodSugarLevel in GlucoseStats.RANGE_MIN..GlucoseStats.RANGE_MAX
                        RangeFilter.HIGH -> it.bloodSugarLevel > GlucoseStats.RANGE_MAX
                    }
                }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun deleteMeasurement(measurement: MeasurementEntity) {
        viewModelScope.launch { repository.delete(measurement) }
    }

    suspend fun getMeasurementsBetween(from: Long, to: Long): List<MeasurementEntity> =
        repository.getBetween(from, to)

    fun saveMeasurement(
        glucoseText: String,
        fastInsulinText: String,
        slowInsulinText: String,
        carbsText: String,
        temporalContext: String?,
        activityText: String,
        notesText: String,
        onOutOfRange: (Int) -> Unit = {}
    ): Int? {
        val glucose = glucoseText.trim().toIntOrNull() ?: return R.string.error_glucose_required
        if (glucose !in GlucoseStats.INPUT_MIN..GlucoseStats.INPUT_MAX) {
            return R.string.error_glucose_range
        }

        val fastInsulin: Double?
        if (fastInsulinText.isBlank()) {
            fastInsulin = null
        } else {
            fastInsulin = fastInsulinText.trim().replace(',', '.').toDoubleOrNull()
            if (fastInsulin == null || fastInsulin !in 0.0..GlucoseStats.INSULIN_MAX) {
                return R.string.error_insulin_invalid
            }
        }

        val slowInsulin: Double?
        if (slowInsulinText.isBlank()) {
            slowInsulin = null
        } else {
            slowInsulin = slowInsulinText.trim().replace(',', '.').toDoubleOrNull()
            if (slowInsulin == null || slowInsulin !in 0.0..GlucoseStats.INSULIN_MAX) {
                return R.string.error_insulin_invalid
            }
        }

        val carbs: Int?
        if (carbsText.isBlank()) {
            carbs = null
        } else {
            carbs = carbsText.trim().toIntOrNull()
            if (carbs == null || carbs < 0 || carbs > GlucoseStats.CARBS_MAX) {
                return R.string.error_carbs_invalid
            }
        }

        val measurement = MeasurementEntity(
            bloodSugarLevel = glucose,
            fastInsulin = fastInsulin,
            slowInsulin = slowInsulin,
            carbohydrates = carbs,
            temporalContext = temporalContext?.takeIf { it.isNotBlank() },
            physicalActivity = activityText.trim().takeIf { it.isNotEmpty() },
            notes = notesText.trim().takeIf { it.isNotEmpty() }
        )
        viewModelScope.launch { repository.insert(measurement) }

        if (glucose !in GlucoseStats.RANGE_MIN..GlucoseStats.RANGE_MAX) {
            onOutOfRange(glucose)
        }
        return null
    }
}
