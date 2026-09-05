package com.xinghan.xingtu.core.util

import java.time.LocalDate

/**
 * Central date formatting helpers. All inputs are Epoch Day values.
 */
object DateFormats {

    /** "2026年9月18日" — full form for pickers and detail headers. */
    fun formatDate(epochDay: Long): String {
        val date = LocalDate.ofEpochDay(epochDay)
        return "${date.year}年${date.monthValue}月${date.dayOfMonth}日"
    }

    /** "9月18日" — short form without the year. */
    fun formatMonthDay(epochDay: Long): String {
        val date = LocalDate.ofEpochDay(epochDay)
        return "${date.monthValue}月${date.dayOfMonth}日"
    }

    /**
     * "9月18日—9月20日" when both dates share a year,
     * otherwise "2025年12月30日—2026年1月2日".
     */
    fun formatDateRange(startEpochDay: Long, endEpochDay: Long): String {
        val start = LocalDate.ofEpochDay(startEpochDay)
        val end = LocalDate.ofEpochDay(endEpochDay)
        return if (start.year == end.year) {
            "${formatMonthDay(startEpochDay)}—${formatMonthDay(endEpochDay)}"
        } else {
            "${formatDate(startEpochDay)}—${formatDate(endEpochDay)}"
        }
    }

    /** Whole days from today until the trip starts (0 when it starts today). */
    fun countdownDays(startEpochDay: Long, todayEpochDay: Long): Int =
        (startEpochDay - todayEpochDay).toInt()
}
