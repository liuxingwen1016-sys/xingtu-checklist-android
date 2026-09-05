package com.xinghan.xingtu.domain.model

/**
 * Trip lifecycle derived from dates; never stored, always computed.
 */
enum class TripStatus {
    UPCOMING,
    ONGOING,
    FINISHED,
}
