package com.xinghan.xingtu.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.xinghan.xingtu.data.local.dao.ChecklistItemDao
import com.xinghan.xingtu.data.local.dao.TripDao
import com.xinghan.xingtu.data.local.entity.ChecklistItemEntity
import com.xinghan.xingtu.data.local.entity.TripEntity

@Database(
    entities = [TripEntity::class, ChecklistItemEntity::class],
    version = 1,
    exportSchema = false,
)
abstract class XingtuDatabase : RoomDatabase() {
    abstract fun tripDao(): TripDao
    abstract fun checklistItemDao(): ChecklistItemDao
}
