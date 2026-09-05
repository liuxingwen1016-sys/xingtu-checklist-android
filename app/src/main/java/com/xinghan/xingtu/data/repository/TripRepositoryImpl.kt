package com.xinghan.xingtu.data.repository

import androidx.room.withTransaction
import com.xinghan.xingtu.core.result.XingtuDataException
import com.xinghan.xingtu.data.local.dao.ChecklistItemDao
import com.xinghan.xingtu.data.local.dao.TripDao
import com.xinghan.xingtu.data.local.db.XingtuDatabase
import com.xinghan.xingtu.data.mapper.toDomain
import com.xinghan.xingtu.data.mapper.toEntity
import com.xinghan.xingtu.data.seed.ChecklistTemplates
import com.xinghan.xingtu.data.seed.DemoDataFactory
import com.xinghan.xingtu.domain.model.ChecklistItem
import com.xinghan.xingtu.domain.model.ChecklistItemInput
import com.xinghan.xingtu.domain.model.DashboardData
import com.xinghan.xingtu.domain.model.DateProvider
import com.xinghan.xingtu.domain.model.PendingItem
import com.xinghan.xingtu.domain.model.Trip
import com.xinghan.xingtu.domain.model.TripCard
import com.xinghan.xingtu.domain.model.TripDetail
import com.xinghan.xingtu.domain.model.TripInput
import com.xinghan.xingtu.domain.model.TripStatistics
import com.xinghan.xingtu.domain.model.TripStatus
import com.xinghan.xingtu.domain.model.TripTemplate
import com.xinghan.xingtu.domain.model.TripThemes
import com.xinghan.xingtu.domain.repository.TripRepository
import com.xinghan.xingtu.domain.usecase.BuildStatisticsUseCase
import com.xinghan.xingtu.domain.usecase.CalculateTripProgressUseCase
import com.xinghan.xingtu.domain.usecase.CalculateTripStatusUseCase
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import java.util.UUID

class TripRepositoryImpl(
    private val db: XingtuDatabase,
    private val tripDao: TripDao,
    private val itemDao: ChecklistItemDao,
    private val dateProvider: DateProvider,
    private val calculateTripStatus: CalculateTripStatusUseCase,
    private val calculateTripProgress: CalculateTripProgressUseCase,
    private val buildStatistics: BuildStatisticsUseCase,
    private val templates: ChecklistTemplates = ChecklistTemplates(),
    private val demoDataFactory: DemoDataFactory = DemoDataFactory(),
) : TripRepository {

    override fun observeTrips(): Flow<List<Trip>> =
        tripDao.observeAll().map { entities -> entities.map { it.toDomain() } }

    override fun observeTripCards(): Flow<List<TripCard>> =
        combine(tripDao.observeAll(), itemDao.observeAll()) { tripEntities, itemEntities ->
            val trips = tripEntities.map { it.toDomain() }
            val items = itemEntities.map { it.toDomain() }
            buildTripCards(trips, items, dateProvider.todayEpochDay())
        }

    override fun observeTripDetail(tripId: String): Flow<TripDetail?> =
        tripDao.observeTripWithItems(tripId).map { it?.toDomain() }

    override fun observeDashboard(): Flow<DashboardData> =
        combine(tripDao.observeAll(), itemDao.observeAll()) { tripEntities, itemEntities ->
            val trips = tripEntities.map { it.toDomain() }
            val items = itemEntities.map { it.toDomain() }
            val today = dateProvider.todayEpochDay()
            val cards = buildTripCards(trips, items, today)

            val activeTrips = cards
                .filter { it.status != TripStatus.FINISHED }
                .sortedWith(
                    compareBy<TripCard>(
                        { if (it.status == TripStatus.ONGOING) 0 else 1 },
                        { if (it.status == TripStatus.ONGOING) it.trip.endDate else it.trip.startDate },
                        { it.trip.createdAt },
                    )
                )
            val pendingItemsByTrip = activeTrips.associate { card ->
                card.trip.id to
                items
                    .filter { it.tripId == card.trip.id && !it.isCompleted }
                    .sortedWith(
                        compareBy(
                            { -it.priority.rawValue },
                            { it.sortOrder },
                            { it.createdAt },
                        )
                    )
                    .take(DASHBOARD_PENDING_LIMIT)
                    .map { PendingItem(trip = card.trip, item = it) }
            }
            val nextTrip = activeTrips.firstOrNull()
            DashboardData(
                nextTrip = nextTrip,
                topPendingItems = nextTrip?.let { pendingItemsByTrip[it.trip.id] }.orEmpty(),
                activeTrips = activeTrips,
                pendingItemsByTrip = pendingItemsByTrip,
            )
        }

    override fun observeStatistics(): Flow<TripStatistics> =
        combine(tripDao.observeAll(), itemDao.observeAll()) { tripEntities, itemEntities ->
            val trips = tripEntities.map { it.toDomain() }
            val items = itemEntities.map { it.toDomain() }
            buildStatistics(trips, items, dateProvider.todayEpochDay())
        }

    override suspend fun createTrip(input: TripInput, template: TripTemplate): String = wrapStorage {
        val now = dateProvider.nowMillis()
        val tripId = UUID.randomUUID().toString()
        val trip = Trip(
            id = tripId,
            title = input.title.trim(),
            destination = input.destination.trim(),
            startDate = input.startDate,
            endDate = input.endDate,
            themeKey = normalizeThemeKey(input.themeKey),
            note = input.note?.trim()?.takeIf { it.isNotEmpty() },
            createdAt = now,
            updatedAt = now,
        )
        val templateItems = templates.itemsFor(template)
        val items = templateItems.mapIndexed { index, templateItem ->
            ChecklistItem(
                id = UUID.randomUUID().toString(),
                tripId = tripId,
                title = templateItem.title,
                category = templateItem.category,
                priority = templateItem.priority,
                note = null,
                isCompleted = false,
                completedAt = null,
                sortOrder = index,
                createdAt = now,
                updatedAt = now,
            )
        }
        db.withTransaction {
            tripDao.insert(trip.toEntity())
            if (items.isNotEmpty()) {
                itemDao.insertAll(items.map { it.toEntity() })
            }
        }
        tripId
    }

    override suspend fun updateTrip(tripId: String, input: TripInput) = wrapStorage {
        val existing = tripDao.getById(tripId)
            ?: throw XingtuDataException(XingtuDataException.Kind.NOT_FOUND, "Trip not found: $tripId")
        val updated = existing.copy(
            title = input.title.trim(),
            destination = input.destination.trim(),
            startDate = input.startDate,
            endDate = input.endDate,
            themeKey = normalizeThemeKey(input.themeKey),
            note = input.note?.trim()?.takeIf { it.isNotEmpty() },
            updatedAt = dateProvider.nowMillis(),
        )
        tripDao.update(updated)
    }

    override suspend fun deleteTrip(tripId: String) = wrapStorage {
        db.withTransaction {
            tripDao.deleteById(tripId)
        }
    }

    override suspend fun saveChecklistItem(input: ChecklistItemInput): String = wrapStorage {
        val now = dateProvider.nowMillis()
        val note = input.note?.trim()?.takeIf { it.isNotEmpty() }
        if (input.itemId == null) {
            val itemId = UUID.randomUUID().toString()
            val nextSortOrder = (itemDao.maxSortOrder(input.tripId) ?: -1) + 1
            val item = ChecklistItem(
                id = itemId,
                tripId = input.tripId,
                title = input.title.trim(),
                category = input.category,
                priority = input.priority,
                note = note,
                isCompleted = false,
                completedAt = null,
                sortOrder = nextSortOrder,
                createdAt = now,
                updatedAt = now,
            )
            itemDao.insert(item.toEntity())
            itemId
        } else {
            val existing = itemDao.getById(input.itemId)
                ?: throw XingtuDataException(
                    XingtuDataException.Kind.NOT_FOUND,
                    "Checklist item not found: ${input.itemId}",
                )
            itemDao.update(
                existing.copy(
                    title = input.title.trim(),
                    category = input.category.rawName,
                    priority = input.priority.rawValue,
                    note = note,
                    updatedAt = now,
                )
            )
            input.itemId
        }
    }

    override suspend fun deleteChecklistItem(itemId: String) = wrapStorage {
        itemDao.deleteById(itemId)
    }

    override suspend fun setChecklistItemCompleted(itemId: String, completed: Boolean) = wrapStorage {
        val now = dateProvider.nowMillis()
        val completedAt = if (completed) now else null
        itemDao.setCompleted(itemId, completed, completedAt, now)
    }

    override suspend fun resetDemoData() = wrapStorage {
        val today = dateProvider.todayEpochDay()
        val now = dateProvider.nowMillis()
        val seeds = demoDataFactory.build(today, now)
        db.withTransaction {
            itemDao.deleteAll()
            tripDao.deleteAll()
            seeds.forEach { seed ->
                val trip = Trip(
                    id = seed.tripId,
                    title = seed.input.title,
                    destination = seed.input.destination,
                    startDate = seed.input.startDate,
                    endDate = seed.input.endDate,
                    themeKey = seed.input.themeKey,
                    note = seed.input.note,
                    createdAt = now,
                    updatedAt = now,
                )
                tripDao.insert(trip.toEntity())
                val items = seed.items.map { demoItem ->
                    ChecklistItem(
                        id = demoItem.itemId,
                        tripId = seed.tripId,
                        title = demoItem.title,
                        category = demoItem.category,
                        priority = demoItem.priority,
                        note = null,
                        isCompleted = demoItem.isCompleted,
                        completedAt = demoItem.completedAt,
                        sortOrder = demoItem.sortOrder,
                        createdAt = now,
                        updatedAt = now,
                    )
                }
                itemDao.insertAll(items.map { it.toEntity() })
            }
        }
    }

    private fun buildTripCards(
        trips: List<Trip>,
        items: List<ChecklistItem>,
        todayEpochDay: Long,
    ): List<TripCard> {
        val itemsByTrip = items.groupBy { it.tripId }
        return trips.map { trip ->
            val tripItems = itemsByTrip[trip.id].orEmpty()
            val completed = tripItems.count { it.isCompleted }
            TripCard(
                trip = trip,
                status = calculateTripStatus(trip, todayEpochDay),
                totalItems = tripItems.size,
                completedItems = completed,
                progressPercent = calculateTripProgress(completed, tripItems.size),
            )
        }
    }

    private fun normalizeThemeKey(key: String): String =
        if (TripThemes.isSupported(key)) key else TripThemes.DEFAULT

    private suspend fun <T> wrapStorage(block: suspend () -> T): T = try {
        block()
    } catch (e: CancellationException) {
        throw e
    } catch (e: XingtuDataException) {
        throw e
    } catch (e: Exception) {
        throw XingtuDataException(XingtuDataException.Kind.STORAGE, cause = e)
    }

    companion object {
        private const val DASHBOARD_PENDING_LIMIT = 3
    }
}
