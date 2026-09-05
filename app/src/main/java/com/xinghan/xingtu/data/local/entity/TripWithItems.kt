package com.xinghan.xingtu.data.local.entity

import androidx.room.Embedded
import androidx.room.Relation

/** A trip with its checklist items, loaded via a Room relation. */
data class TripWithItems(
    @Embedded val trip: TripEntity,
    @Relation(parentColumn = "id", entityColumn = "trip_id")
    val items: List<ChecklistItemEntity>,
)
