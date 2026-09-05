package com.xinghan.xingtu.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.xinghan.xingtu.data.local.entity.TripEntity
import com.xinghan.xingtu.data.local.entity.TripWithItems
import kotlinx.coroutines.flow.Flow

@Dao
interface TripDao {

    @Query("SELECT * FROM trip ORDER BY start_date ASC")
    fun observeAll(): Flow<List<TripEntity>>

    @Query("SELECT * FROM trip WHERE id = :id")
    fun observeById(id: String): Flow<TripEntity?>

    @Transaction
    @Query("SELECT * FROM trip WHERE id = :id")
    fun observeTripWithItems(id: String): Flow<TripWithItems?>

    @Query("SELECT * FROM trip WHERE id = :id")
    suspend fun getById(id: String): TripEntity?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(entity: TripEntity)

    @Update
    suspend fun update(entity: TripEntity)

    @Query("DELETE FROM trip WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("SELECT COUNT(*) FROM trip")
    suspend fun countTrips(): Int

    @Query("DELETE FROM trip")
    suspend fun deleteAll()
}
