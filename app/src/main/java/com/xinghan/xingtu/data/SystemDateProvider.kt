package com.xinghan.xingtu.data

import com.xinghan.xingtu.domain.model.DateProvider
import java.time.LocalDate
import java.time.ZoneId

/** Production [DateProvider] backed by the system clock. */
class SystemDateProvider : DateProvider {

    override fun todayEpochDay(): Long =
        LocalDate.now(ZoneId.systemDefault()).toEpochDay()

    override fun nowMillis(): Long = System.currentTimeMillis()
}
