package com.xinghan.xingtu.domain.model

data class ChecklistItem(
    val id: String,
    val tripId: String,
    val title: String,
    val category: ChecklistCategory,
    val priority: ChecklistPriority,
    val note: String?,
    val isCompleted: Boolean,
    val completedAt: Long?,
    val sortOrder: Int,
    val createdAt: Long,
    val updatedAt: Long,
)
