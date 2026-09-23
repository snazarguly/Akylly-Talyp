package com.example.core.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.math.roundToInt

/**
 * Small date helpers shared by the schedule screens.
 * Uses [Calendar] because `java.time` needs API 26 and the app supports API 24.
 */
object CampusCalendar {

    /** Today's day of week where 1 = Monday … 7 = Sunday (same convention as `ScheduleItem.dayOfWeek`). */
    fun todayDayOfWeek(now: Calendar = Calendar.getInstance()): Int =
        (now.get(Calendar.DAY_OF_WEEK) + 5) % 7 + 1

    /** Day of month of the given weekday (1 = Monday) within the current week. */
    fun dayOfMonthInCurrentWeek(dayOfWeek: Int, now: Calendar = Calendar.getInstance()): Int {
        val date = now.clone() as Calendar
        date.add(Calendar.DAY_OF_MONTH, dayOfWeek - todayDayOfWeek(now))
        return date.get(Calendar.DAY_OF_MONTH)
    }

    /** Parses "HH:mm" into minutes since midnight, or null when malformed. */
    fun minutesOfDay(time: String): Int? {
        val parts = time.split(":")
        if (parts.size != 2) return null
        val hours = parts[0].toIntOrNull() ?: return null
        val minutes = parts[1].toIntOrNull() ?: return null
        return hours * 60 + minutes
    }

    /** Whole days from today until [dateIso] ("yyyy-MM-dd"); negative when it is in the past, null when malformed. */
    fun daysUntil(dateIso: String, now: Calendar = Calendar.getInstance()): Int? {
        val date = runCatching { SimpleDateFormat("yyyy-MM-dd", Locale.ROOT).parse(dateIso) }.getOrNull()
            ?: return null
        val todayStart = (now.clone() as Calendar).apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
        // Rounding absorbs the one-hour shift on daylight-saving days.
        return ((date.time - todayStart) / DAY_MILLIS.toDouble()).roundToInt()
    }

    private const val DAY_MILLIS = 24 * 60 * 60 * 1000L
}
