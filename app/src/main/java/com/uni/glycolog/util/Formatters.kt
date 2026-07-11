package com.uni.glycolog.util

import android.text.format.DateUtils
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object Formatters {

    fun formatDateTime(timestamp: Long): String =
        SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(timestamp))

    fun formatDate(timestamp: Long): String =
        SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(timestamp))

    fun relativeTime(timestamp: Long): String =
        DateUtils.getRelativeTimeSpanString(timestamp).toString()

    fun formatDose(dose: Double): String =
        if (dose % 1.0 == 0.0) dose.toInt().toString() else dose.toString()

    fun formatDecimal(value: Double): String =
        String.format(Locale.getDefault(), "%.1f", value)
}
