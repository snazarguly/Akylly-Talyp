package com.example.core.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.util.Calendar

class CampusCalendarTest {

    private fun date(year: Int, month: Int, day: Int, hour: Int = 12): Calendar =
        Calendar.getInstance().apply { clear(); set(year, month, day, hour, 0) }

    @Test
    fun todayDayOfWeekUsesMondayAsOne() {
        assertEquals(1, CampusCalendar.todayDayOfWeek(date(2026, Calendar.SEPTEMBER, 21))) // Monday
        assertEquals(3, CampusCalendar.todayDayOfWeek(date(2026, Calendar.SEPTEMBER, 23))) // Wednesday
        assertEquals(7, CampusCalendar.todayDayOfWeek(date(2026, Calendar.SEPTEMBER, 27))) // Sunday
    }

    @Test
    fun dayOfMonthInCurrentWeekCrossesMonthBoundary() {
        val wednesday = date(2026, Calendar.SEPTEMBER, 30)
        assertEquals(28, CampusCalendar.dayOfMonthInCurrentWeek(1, wednesday))
        assertEquals(3, CampusCalendar.dayOfMonthInCurrentWeek(6, wednesday)) // Saturday, Oct 3
    }

    @Test
    fun daysUntilCountsWholeDays() {
        val now = date(2026, Calendar.SEPTEMBER, 23, hour = 23)
        assertEquals(0, CampusCalendar.daysUntil("2026-09-23", now))
        assertEquals(1, CampusCalendar.daysUntil("2026-09-24", now))
        assertEquals(-3, CampusCalendar.daysUntil("2026-09-20", now))
        assertNull(CampusCalendar.daysUntil("not-a-date", now))
    }

    @Test
    fun minutesOfDayParsesTime() {
        assertEquals(9 * 60 + 5, CampusCalendar.minutesOfDay("09:05"))
        assertNull(CampusCalendar.minutesOfDay("9h"))
    }
}
