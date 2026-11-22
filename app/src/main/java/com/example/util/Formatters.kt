package com.example.util

import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object Formatters {
    private val phLocale = Locale("en", "PH")

    fun formatPeso(amount: Double, showSign: Boolean = false): String {
        val formatter = NumberFormat.getNumberInstance(phLocale).apply {
            minimumFractionDigits = 2
            maximumFractionDigits = 2
        }
        val absFormatted = formatter.format(kotlin.math.abs(amount))
        return when {
            amount < 0 -> "-₱$absFormatted"
            showSign && amount > 0 -> "+₱$absFormatted"
            else -> "₱$absFormatted"
        }
    }

    fun formatPesoCompact(amount: Double): String {
        val formatter = NumberFormat.getNumberInstance(phLocale).apply {
            val hasCents = (kotlin.math.abs(amount) * 100).toLong() % 100 != 0L
            minimumFractionDigits = if (hasCents) 2 else 0
            maximumFractionDigits = 2
        }
        val absFormatted = formatter.format(kotlin.math.abs(amount))
        return if (amount < 0) "-₱$absFormatted" else "₱$absFormatted"
    }

    fun formatFullDate(millis: Long): String {
        val sdf = SimpleDateFormat("EEEE, MMM d, yyyy", Locale.getDefault())
        return sdf.format(Date(millis))
    }

    fun formatMediumDate(millis: Long): String {
        val sdf = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
        return sdf.format(Date(millis))
    }

    fun formatShortDate(millis: Long): String {
        val sdf = SimpleDateFormat("MMM d", Locale.getDefault())
        return sdf.format(Date(millis))
    }

    fun formatDayLabel(millis: Long): String {
        val sdf = SimpleDateFormat("EEE", Locale.getDefault())
        return sdf.format(Date(millis))
    }

    fun formatRelativeDateHeader(millis: Long, nowMillis: Long = System.currentTimeMillis()): String {
        val targetCal = Calendar.getInstance().apply { timeInMillis = millis }
        val todayCal = Calendar.getInstance().apply { timeInMillis = nowMillis }
        val yesterdayCal = Calendar.getInstance().apply {
            timeInMillis = nowMillis
            add(Calendar.DAY_OF_YEAR, -1)
        }
        return when {
            isSameDay(targetCal, todayCal) -> "Today • ${formatShortDate(millis)}"
            isSameDay(targetCal, yesterdayCal) -> "Yesterday • ${formatShortDate(millis)}"
            else -> SimpleDateFormat("EEE, MMM d, yyyy", Locale.getDefault()).format(Date(millis))
        }
    }

    fun startOfDayMillis(millis: Long = System.currentTimeMillis()): Long {
        return Calendar.getInstance().apply {
            timeInMillis = millis
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

    fun endOfDayMillis(millis: Long = System.currentTimeMillis()): Long {
        return Calendar.getInstance().apply {
            timeInMillis = millis
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }.timeInMillis
    }

    fun startOfWeekMillis(millis: Long = System.currentTimeMillis()): Long {
        return Calendar.getInstance().apply {
            timeInMillis = millis
            firstDayOfWeek = Calendar.MONDAY
            set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

    fun startOfMonthMillis(millis: Long = System.currentTimeMillis()): Long {
        return Calendar.getInstance().apply {
            timeInMillis = millis
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

    fun daysRemaining(targetDateMillis: Long, nowMillis: Long = System.currentTimeMillis()): Int {
        val startNow = startOfDayMillis(nowMillis)
        val startTarget = startOfDayMillis(targetDateMillis)
        val diff = startTarget - startNow
        return (diff / (24L * 60L * 60L * 1000L)).toInt()
    }

    private fun isSameDay(a: Calendar, b: Calendar): Boolean {
        return a.get(Calendar.YEAR) == b.get(Calendar.YEAR) &&
            a.get(Calendar.DAY_OF_YEAR) == b.get(Calendar.DAY_OF_YEAR)
    }
}
