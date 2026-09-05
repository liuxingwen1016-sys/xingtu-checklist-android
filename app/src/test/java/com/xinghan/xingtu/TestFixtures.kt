package com.xinghan.xingtu

import com.xinghan.xingtu.domain.model.ChecklistCategory
import com.xinghan.xingtu.domain.model.ChecklistItem
import com.xinghan.xingtu.domain.model.ChecklistPriority
import com.xinghan.xingtu.domain.model.Trip

/** Shared builders for domain objects in unit tests. */
object TestFixtures {

    fun trip(
        id: String = "trip-1",
        title: String = "测试行程",
        destination: String = "测试目的地",
        startDate: Long = 0L,
        endDate: Long = 1L,
        themeKey: String = "indigo",
        note: String? = null,
        createdAt: Long = 1_000L,
        updatedAt: Long = 1_000L,
    ): Trip = Trip(
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

    fun item(
        id: String = "item-1",
        tripId: String = "trip-1",
        title: String = "测试事项",
        category: ChecklistCategory = ChecklistCategory.OTHER,
        priority: ChecklistPriority = ChecklistPriority.NORMAL,
        note: String? = null,
        isCompleted: Boolean = false,
        completedAt: Long? = null,
        sortOrder: Int = 0,
        createdAt: Long = 1_000L,
        updatedAt: Long = 1_000L,
    ): ChecklistItem = ChecklistItem(
        id = id,
        tripId = tripId,
        title = title,
        category = category,
        priority = priority,
        note = note,
        isCompleted = isCompleted,
        completedAt = completedAt,
        sortOrder = sortOrder,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )
}
