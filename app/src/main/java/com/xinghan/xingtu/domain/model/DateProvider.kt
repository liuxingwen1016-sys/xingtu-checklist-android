package com.xinghan.xingtu.domain.model

/**
 * Provides the current date/time for the application. Injected so that
 * status calculations and tests never depend directly on the system clock.
 */
interface DateProvider {
    /** Today as Epoch Day (days since 1970-01-01), in the system timezone. */
    fun todayEpochDay(): Long

    /** Current wall-clock time in UTC epoch milliseconds. */
    fun nowMillis(): Long
}
