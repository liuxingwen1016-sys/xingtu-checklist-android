package com.xinghan.xingtu.domain.model

/** User input for creating or editing a trip. */
data class TripInput(
    val title: String,
    val destination: String,
    val startDate: Long,
    val endDate: Long,
    val themeKey: String,
    val note: String?,
)

/** User input for creating or editing a checklist item. */
data class ChecklistItemInput(
    val itemId: String?,
    val tripId: String,
    val title: String,
    val category: ChecklistCategory,
    val priority: ChecklistPriority,
    val note: String?,
)
