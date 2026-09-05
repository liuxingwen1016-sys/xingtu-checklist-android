package com.xinghan.xingtu

import com.xinghan.xingtu.TestFixtures.trip
import com.xinghan.xingtu.domain.model.TripCard
import com.xinghan.xingtu.domain.model.TripStatus
import com.xinghan.xingtu.domain.usecase.BuildShareSummaryUseCase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BuildShareSummaryUseCaseTest {

    private val useCase = BuildShareSummaryUseCase { start, end -> "RANGE($start,$end)" }

    private val card = TripCard(
        trip = trip(
            id = "t1",
            title = "深圳技术交流行程",
            destination = "深圳",
            startDate = 10,
            endDate = 12,
        ),
        status = TripStatus.UPCOMING,
        totalItems = 10,
        completedItems = 6,
        progressPercent = 60,
    )

    @Test
    fun `summary contains name destination dates and progress`() {
        val text = useCase(card)
        assertTrue(text.contains("深圳技术交流行程"))
        assertTrue(text.contains("深圳"))
        assertTrue(text.contains("RANGE(10,12)"))
        assertTrue(text.contains("6/10"))
        assertTrue(text.contains("60%"))
    }

    @Test
    fun `summary progress line format is exact`() {
        val text = useCase(card)
        assertTrue(text.contains("完成进度：6/10（60%）"))
    }

    @Test
    fun `empty checklist shows zero progress without NaN`() {
        val emptyCard = card.copy(totalItems = 0, completedItems = 0, progressPercent = 0)
        val text = useCase(emptyCard)
        assertTrue(text.contains("完成进度：0/0（0%）"))
    }

    @Test
    fun `summary starts with the brand header`() {
        assertTrue(useCase(card).startsWith("【星途清单】行程分享"))
    }

    @Test
    fun `real date formatter produces human readable range`() {
        val realUseCase = BuildShareSummaryUseCase { start, end ->
            com.xinghan.xingtu.core.util.DateFormats.formatDateRange(start, end)
        }
        val day = java.time.LocalDate.of(2026, 9, 18).toEpochDay()
        val trip = trip(title = "深圳技术交流行程", destination = "深圳", startDate = day, endDate = day + 2)
        val text = realUseCase(
            TripCard(trip, TripStatus.UPCOMING, 10, 6, 60)
        )
        assertEquals(true, text.contains("9月18日—9月20日"))
    }
}
