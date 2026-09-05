package com.xinghan.xingtu.data.mapper

import com.xinghan.xingtu.data.local.entity.ChecklistItemEntity
import com.xinghan.xingtu.data.local.entity.TripEntity
import com.xinghan.xingtu.data.local.entity.TripWithItems
import com.xinghan.xingtu.domain.model.ChecklistCategory
import com.xinghan.xingtu.domain.model.ChecklistItem
import com.xinghan.xingtu.domain.model.ChecklistPriority
import com.xinghan.xingtu.domain.model.Trip
import com.xinghan.xingtu.domain.model.TripDetail

fun TripEntity.toDomain(): Trip = Trip(
    id = id,
    title = title,
    destination = destination,
    startDate = startDate,
    endDate = endDate,
    themeKey = themeKey,
    note = note,
    createdAt = createdAt,
    updatedAt = updatedAt,
)

fun Trip.toEntity(): TripEntity = TripEntity(
    id = id,
    title = title,
    destination = destination,
    startDate = startDate,
    endDate = endDate,
    themeKey = themeKey,
    note = note,
    createdAt = createdAt,
    updatedAt = updatedAt,
)

fun ChecklistItemEntity.toDomain(): ChecklistItem = ChecklistItem(
    id = id,
    tripId = tripId,
    title = title,
    category = ChecklistCategory.fromRaw(category),
    priority = ChecklistPriority.fromRaw(priority),
    note = note,
    isCompleted = isCompleted,
    completedAt = completedAt,
    sortOrder = sortOrder,
    createdAt = createdAt,
    updatedAt = updatedAt,
)

fun ChecklistItem.toEntity(): ChecklistItemEntity = ChecklistItemEntity(
    id = id,
    tripId = tripId,
    title = title,
    category = category.rawName,
    priority = priority.rawValue,
    note = note,
    isCompleted = isCompleted,
    completedAt = completedAt,
    sortOrder = sortOrder,
    createdAt = createdAt,
    updatedAt = updatedAt,
)

/**
 * Maps a Room relation to a domain [TripDetail], sorting items by the fixed
 * category order, then: incomplete first, important first, sortOrder ASC,
 * createdAt ASC.
 */
fun TripWithItems.toDomain(): TripDetail = TripDetail(
    trip = trip.toDomain(),
    items = items
        .map { it.toDomain() }
        .sortedWith(
            compareBy(
                { ChecklistCategory.DISPLAY_ORDER.indexOf(it.category) },
                { it.isCompleted },
                { -it.priority.rawValue },
                { it.sortOrder },
                { it.createdAt },
            )
        ),
)
