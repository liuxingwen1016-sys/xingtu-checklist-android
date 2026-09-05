package com.xinghan.xingtu.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "checklist_item",
    foreignKeys = [
        ForeignKey(
            entity = TripEntity::class,
            parentColumns = ["id"],
            childColumns = ["trip_id"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [
        Index(value = ["trip_id"], name = "index_checklist_item_trip_id"),
        Index(value = ["completed_at"], name = "index_checklist_item_completed_at"),
        Index(value = ["trip_id", "category", "sort_order"], name = "index_checklist_item_trip_category_sort"),
    ],
)
data class ChecklistItemEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "trip_id") val tripId: String,
    @ColumnInfo(name = "title") val title: String,
    @ColumnInfo(name = "category") val category: String,
    @ColumnInfo(name = "priority") val priority: Int,
    @ColumnInfo(name = "note") val note: String?,
    @ColumnInfo(name = "is_completed") val isCompleted: Boolean,
    @ColumnInfo(name = "completed_at") val completedAt: Long?,
    @ColumnInfo(name = "sort_order") val sortOrder: Int,
    @ColumnInfo(name = "created_at") val createdAt: Long,
    @ColumnInfo(name = "updated_at") val updatedAt: Long,
)
