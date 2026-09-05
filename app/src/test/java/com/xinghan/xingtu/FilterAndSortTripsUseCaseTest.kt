package com.xinghan.xingtu

import com.xinghan.xingtu.TestFixtures.trip
import com.xinghan.xingtu.domain.model.TripCard
import com.xinghan.xingtu.domain.model.TripStatus
import com.xinghan.xingtu.domain.usecase.CalculateTripStatusUseCase
import com.xinghan.xingtu.domain.usecase.FilterAndSortTripsUseCase
import com.xinghan.xingtu.domain.usecase.TripFilter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FilterAndSortTripsUseCaseTest {

    private val statusUseCase = CalculateTripStatusUseCase()
    private val useCase = FilterAndSortTripsUseCase()
    private val today = 100L

    private fun card(
        id: String,
        title: String,
        destination: String,
        start: Long,
        end: Long,
        updatedAt: Long = 1_000L,
    ): TripCard {
        val trip = trip(
            id = id,
            title = title,
            destination = destination,
            startDate = start,
            endDate = end,
            updatedAt = updatedAt,
        )
        return TripCard(
            trip = trip,
            status = statusUseCase(trip, today),
            totalItems = 0,
            completedItems = 0,
            progressPercent = 0,
        )
    }

    private val upcomingA = card("a", "Beijing Trip", "北京", start = 110, end = 112)
    private val upcomingB = card("b", "广州出差", "Guangzhou", start = 105, end = 106)
    private val ongoing = card("c", "进行中行程", "上海", start = 99, end = 101)
    private val finishedX = card("x", "已结束一号", "杭州", start = 80, end = 82)
    private val finishedY = card("y", "已结束二号", "南京", start = 90, end = 95)
    private val allCards = listOf(upcomingA, upcomingB, ongoing, finishedX, finishedY)

    @Test
    fun `query matches title case-insensitively`() {
        val result = useCase(allCards, query = "beijing", filter = TripFilter.ALL)
        assertEquals(listOf("a"), result.map { it.trip.id })
    }

    @Test
    fun `query matches destination and trims whitespace`() {
        val result = useCase(allCards, query = "  guangzhou  ", filter = TripFilter.ALL)
        assertEquals(listOf("b"), result.map { it.trip.id })
    }

    @Test
    fun `query with no match returns empty`() {
        assertTrue(useCase(allCards, "不存在的内容", TripFilter.ALL).isEmpty())
    }

    @Test
    fun `filter by status`() {
        assertEquals(
            listOf("c"),
            useCase(allCards, "", TripFilter.ONGOING).map { it.trip.id },
        )
        assertEquals(
            2,
            useCase(allCards, "", TripFilter.FINISHED).size,
        )
        assertEquals(
            2,
            useCase(allCards, "", TripFilter.UPCOMING).size,
        )
    }

    @Test
    fun `all filter orders active by start date then finished by end date desc`() {
        val result = useCase(allCards, "", TripFilter.ALL)
        // Active: ongoing(c, start 99) then upcoming b (105) then a (110)
        // Finished: y (end 95) then x (end 82)
        assertEquals(listOf("c", "b", "a", "y", "x"), result.map { it.trip.id })
    }

    @Test
    fun `same start date breaks tie by updated at desc`() {
        val old = card("old", "旧更新", "北京", start = 105, end = 106, updatedAt = 1_000)
        val new = card("new", "新更新", "北京", start = 105, end = 106, updatedAt = 2_000)
        val result = useCase(listOf(old, new), "", TripFilter.UPCOMING)
        assertEquals(listOf("new", "old"), result.map { it.trip.id })
    }

    @Test
    fun `status enum round trip`() {
        assertEquals(TripFilter.ALL, TripFilter.fromRaw(null))
        assertEquals(TripFilter.FINISHED, TripFilter.fromRaw("finished"))
        assertEquals(TripFilter.ALL, TripFilter.fromRaw("unknown"))
    }
}
