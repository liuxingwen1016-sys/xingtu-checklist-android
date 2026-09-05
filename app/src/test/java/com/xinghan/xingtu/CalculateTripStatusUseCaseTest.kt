package com.xinghan.xingtu

import com.xinghan.xingtu.TestFixtures.trip
import com.xinghan.xingtu.domain.model.TripStatus
import com.xinghan.xingtu.domain.usecase.CalculateTripStatusUseCase
import org.junit.Assert.assertEquals
import org.junit.Test

class CalculateTripStatusUseCaseTest {

    private val useCase = CalculateTripStatusUseCase()

    @Test
    fun `today before start is upcoming`() {
        val trip = trip(startDate = 10, endDate = 12)
        assertEquals(TripStatus.UPCOMING, useCase(trip, todayEpochDay = 9))
    }

    @Test
    fun `today equals start is ongoing`() {
        val trip = trip(startDate = 10, endDate = 12)
        assertEquals(TripStatus.ONGOING, useCase(trip, todayEpochDay = 10))
    }

    @Test
    fun `today between start and end is ongoing`() {
        val trip = trip(startDate = 10, endDate = 12)
        assertEquals(TripStatus.ONGOING, useCase(trip, todayEpochDay = 11))
    }

    @Test
    fun `today equals end is ongoing`() {
        val trip = trip(startDate = 10, endDate = 12)
        assertEquals(TripStatus.ONGOING, useCase(trip, todayEpochDay = 12))
    }

    @Test
    fun `today after end is finished`() {
        val trip = trip(startDate = 10, endDate = 12)
        assertEquals(TripStatus.FINISHED, useCase(trip, todayEpochDay = 13))
    }

    @Test
    fun `single day trip is ongoing only on that day`() {
        val trip = trip(startDate = 10, endDate = 10)
        assertEquals(TripStatus.UPCOMING, useCase(trip, 9))
        assertEquals(TripStatus.ONGOING, useCase(trip, 10))
        assertEquals(TripStatus.FINISHED, useCase(trip, 11))
    }
}
