package com.xinghan.xingtu.domain.usecase

import com.xinghan.xingtu.domain.model.TripCard
import com.xinghan.xingtu.domain.model.TripStatus

/** Status filter for the trips list. */
enum class TripFilter(val rawName: String) {
    ALL("all"),
    UPCOMING("upcoming"),
    ONGOING("ongoing"),
    FINISHED("finished"),
    ;

    companion object {
        fun fromRaw(raw: String?): TripFilter =
            entries.firstOrNull { it.rawName == raw } ?: ALL
    }
}

/**
 * Filters trips by status and free-text query, then sorts:
 * - active (UPCOMING/ONGOING) first by startDate ASC, updatedAt DESC;
 * - FINISHED after them by endDate DESC, updatedAt DESC.
 * The query matches title or destination, case-insensitive, trimmed.
 */
class FilterAndSortTripsUseCase {

    operator fun invoke(cards: List<TripCard>, query: String, filter: TripFilter): List<TripCard> {
        val trimmed = query.trim()
        val filtered = cards.filter { card ->
            val statusMatch = when (filter) {
                TripFilter.ALL -> true
                TripFilter.UPCOMING -> card.status == TripStatus.UPCOMING
                TripFilter.ONGOING -> card.status == TripStatus.ONGOING
                TripFilter.FINISHED -> card.status == TripStatus.FINISHED
            }
            val queryMatch = trimmed.isEmpty() ||
                card.trip.title.contains(trimmed, ignoreCase = true) ||
                card.trip.destination.contains(trimmed, ignoreCase = true)
            statusMatch && queryMatch
        }
        val active = filtered
            .filter { it.status != TripStatus.FINISHED }
            .sortedWith(compareBy({ it.trip.startDate }, { -it.trip.updatedAt }))
        val finished = filtered
            .filter { it.status == TripStatus.FINISHED }
            .sortedWith(compareBy({ -it.trip.endDate }, { -it.trip.updatedAt }))
        return active + finished
    }
}
