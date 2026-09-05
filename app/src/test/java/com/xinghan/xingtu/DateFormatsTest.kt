package com.xinghan.xingtu

import com.xinghan.xingtu.core.util.DateFormats
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class DateFormatsTest {

    @Test
    fun `full date uses chinese year month day`() {
        val day = LocalDate.of(2026, 9, 18).toEpochDay()
        assertEquals("2026年9月18日", DateFormats.formatDate(day))
    }

    @Test
    fun `month day omits year`() {
        val day = LocalDate.of(2026, 9, 18).toEpochDay()
        assertEquals("9月18日", DateFormats.formatMonthDay(day))
    }

    @Test
    fun `same year range omits years`() {
        val start = LocalDate.of(2026, 9, 18).toEpochDay()
        val end = LocalDate.of(2026, 9, 20).toEpochDay()
        assertEquals("9月18日—9月20日", DateFormats.formatDateRange(start, end))
    }

    @Test
    fun `cross year range keeps both years`() {
        val start = LocalDate.of(2025, 12, 30).toEpochDay()
        val end = LocalDate.of(2026, 1, 2).toEpochDay()
        assertEquals("2025年12月30日—2026年1月2日", DateFormats.formatDateRange(start, end))
    }

    @Test
    fun `countdown is whole days until start`() {
        assertEquals(7, DateFormats.countdownDays(startEpochDay = 107, todayEpochDay = 100))
        assertEquals(0, DateFormats.countdownDays(100, 100))
    }
}
