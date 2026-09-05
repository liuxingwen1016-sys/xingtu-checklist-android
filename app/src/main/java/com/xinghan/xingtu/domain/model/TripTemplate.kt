package com.xinghan.xingtu.domain.model

/**
 * Checklist templates offered when creating a trip.
 * CUSTOM means "no preset items".
 */
enum class TripTemplate(val rawName: String) {
    BUSINESS("business"),
    WEEKEND("weekend"),
    CUSTOM("custom"),
    ;

    companion object {
        fun fromRaw(raw: String?): TripTemplate =
            entries.firstOrNull { it.rawName == raw } ?: BUSINESS
    }
}
