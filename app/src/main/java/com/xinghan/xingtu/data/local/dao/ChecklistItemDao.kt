package com.xinghan.xingtu.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.xinghan.xingtu.data.local.entity.ChecklistItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ChecklistItemDao {

    @Query("SELECT * FROM checklist_item WHERE trip_id = :tripId")
    fun observeByTripId(tripId: String): Flow<List<ChecklistItemEntity>>

    @Query("SELECT * FROM checklist_item")
    fun observeAll(): Flow<List<ChecklistItemEntity>>

    @Query("SELECT * FROM checklist_item WHERE id = :id")
    suspend fun getById(id: String): ChecklistItemEntity?

    @Query("SELECT MAX(sort_order) FROM checklist_item WHERE trip_id = :tripId")
    suspend fun maxSortOrder(tripId: String): Int?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(item: ChecklistItemEntity)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertAll(items: List<ChecklistItemEntity>)

    @Update
    suspend fun update(item: ChecklistItemEntity)

    @Query("DELETE FROM checklist_item WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query(
        "UPDATE checklist_item SET is_completed = :completed, " +
            "completed_at = :completedAt, updated_at = :updatedAt WHERE id = :id"
    )
    suspend fun setCompleted(id: String, completed: Boolean, completedAt: Long?, updatedAt: Long)

    @Query("SELECT * FROM checklist_item WHERE completed_at BETWEEN :startMillis AND :endMillis")
    fun observeCompletedBetween(startMillis: Long, endMillis: Long): Flow<List<ChecklistItemEntity>>

    @Query("DELETE FROM checklist_item")
    suspend fun deleteAll()
}
