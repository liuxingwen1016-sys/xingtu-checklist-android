package com.xinghan.xingtu.domain.repository

import com.xinghan.xingtu.domain.model.ChecklistItemInput
import com.xinghan.xingtu.domain.model.DashboardData
import com.xinghan.xingtu.domain.model.Trip
import com.xinghan.xingtu.domain.model.TripCard
import com.xinghan.xingtu.domain.model.TripDetail
import com.xinghan.xingtu.domain.model.TripInput
import com.xinghan.xingtu.domain.model.TripStatistics
import com.xinghan.xingtu.domain.model.TripTemplate
import kotlinx.coroutines.flow.Flow

interface TripRepository {
    fun observeTrips(): Flow<List<Trip>>
    fun observeTripCards(): Flow<List<TripCard>>
    fun observeTripDetail(tripId: String): Flow<TripDetail?>
    fun observeDashboard(): Flow<DashboardData>
    fun observeStatistics(): Flow<TripStatistics>
    suspend fun createTrip(input: TripInput, template: TripTemplate): String
    suspend fun updateTrip(tripId: String, input: TripInput)
    suspend fun deleteTrip(tripId: String)
    suspend fun saveChecklistItem(input: ChecklistItemInput): String
    suspend fun deleteChecklistItem(itemId: String)
    suspend fun setChecklistItemCompleted(itemId: String, completed: Boolean)
    suspend fun resetDemoData()
}
