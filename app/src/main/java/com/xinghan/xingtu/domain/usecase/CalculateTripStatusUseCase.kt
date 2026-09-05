package com.xinghan.xingtu.domain.usecase

import com.xinghan.xingtu.domain.model.Trip
import com.xinghan.xingtu.domain.model.TripStatus

/**
 * Computes the trip status from dates:
 * today < start -> UPCOMING; start <= today <= end -> ONGOING; else FINISHED.
 */
class CalculateTripStatusUseCase {

    operator fun invoke(trip: Trip, todayEpochDay: Long): TripStatus = when {
        todayEpochDay < trip.startDate -> TripStatus.UPCOMING
        todayEpochDay <= trip.endDate -> TripStatus.ONGOING
        else -> TripStatus.FINISHED
    }
}
