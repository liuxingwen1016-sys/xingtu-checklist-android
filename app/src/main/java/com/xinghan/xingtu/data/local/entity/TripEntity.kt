package com.xinghan.xingtu.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "trip",
    indices = [
        Index(value = ["start_date"], name = "index_trip_start_date"),
        Index(value = ["end_date"], name = "index_trip_end_date"),
    ],
)
data class TripEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "title") val title: String,
    @ColumnInfo(name = "destination") val destination: String,
    @ColumnInfo(name = "start_date") val startDate: Long,
    @ColumnInfo(name = "end_date") val endDate: Long,
    @ColumnInfo(name = "theme_key") val themeKey: String,
    @ColumnInfo(name = "note") val note: String?,
    @ColumnInfo(name = "created_at") val createdAt: Long,
    @ColumnInfo(name = "updated_at") val updatedAt: Long,
)
