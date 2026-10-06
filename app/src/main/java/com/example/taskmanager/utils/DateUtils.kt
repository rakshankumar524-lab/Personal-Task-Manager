package com.example.taskmanager.utils

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object DateUtils {
    private val isoFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val displayFormat = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
    private val dayMonthFormat = SimpleDateFormat("EEE d", Locale.getDefault())

    fun todayString(): String {
        return isoFormat.format(Date())
    }

    fun tomorrowString(): String {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, 1)
        return isoFormat.format(calendar.time)
    }

    fun formatDateDisplay(dateStr: String): String {
        if (dateStr.isBlank()) return ""
        if (dateStr == todayString()) return "Today"
        if (dateStr == tomorrowString()) return "Tomorrow"
        return try {
            val date = isoFormat.parse(dateStr)
            date?.let { displayFormat.format(it) } ?: dateStr
        } catch (e: Exception) {
            dateStr
        }
    }

    fun isToday(dateStr: String): Boolean {
        return dateStr == todayString()
    }

    fun isOverdue(dateStr: String): Boolean {
        if (dateStr.isBlank()) return false
        val today = todayString()
        return dateStr < today
    }

    data class DateOption(
        val key: String,
        val label: String,
        val isSpecial: Boolean = false
    )

    fun getDateOptions(): List<DateOption> {
        val options = mutableListOf<DateOption>()
        options.add(DateOption(key = "ALL", label = "All", isSpecial = true))
        options.add(DateOption(key = "OVERDUE", label = "Overdue", isSpecial = true))
        options.add(DateOption(key = todayString(), label = "Today", isSpecial = true))
        options.add(DateOption(key = tomorrowString(), label = "Tomorrow", isSpecial = true))

        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, 2) // Start from day after tomorrow

        for (i in 0..7) {
            val dateKey = isoFormat.format(calendar.time)
            val label = dayMonthFormat.format(calendar.time)
            options.add(DateOption(key = dateKey, label = label))
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }

        return options
    }

    fun formatGreetingTime(): String {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        return when (hour) {
            in 4..11 -> "Good Morning"
            in 12..16 -> "Good Afternoon"
            in 17..22 -> "Good Evening"
            else -> "Hello"
        }
    }
}
