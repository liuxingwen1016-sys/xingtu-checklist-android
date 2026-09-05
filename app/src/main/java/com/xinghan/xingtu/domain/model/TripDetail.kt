package com.xinghan.xingtu.domain.model

/** A trip together with its full checklist. */
data class TripDetail(
    val trip: Trip,
    val items: List<ChecklistItem>,
)

/** Presentation-ready summary of a trip for list/home/widget surfaces. */
data class TripCard(
    val trip: Trip,
    val status: TripStatus,
    val totalItems: Int,
    val completedItems: Int,
    val progressPercent: Int,
)

/** Home dashboard aggregate. */
data class DashboardData(
    val nextTrip: TripCard?,
    val topPendingItems: List<PendingItem>,
    val activeTrips: List<TripCard> = listOfNotNull(nextTrip),
    val pendingItemsByTrip: Map<String, List<PendingItem>> =
        topPendingItems.groupBy { it.trip.id },
)

/** An incomplete checklist item shown on the home dashboard. */
data class PendingItem(
    val trip: Trip,
    val item: ChecklistItem,
)
