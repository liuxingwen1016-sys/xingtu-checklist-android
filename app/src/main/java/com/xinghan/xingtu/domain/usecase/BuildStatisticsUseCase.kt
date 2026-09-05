package com.xinghan.xingtu.domain.usecase

import com.xinghan.xingtu.domain.model.CategoryStat
import com.xinghan.xingtu.domain.model.ChecklistCategory
import com.xinghan.xingtu.domain.model.ChecklistItem
import com.xinghan.xingtu.domain.model.DayCompletion
import com.xinghan.xingtu.domain.model.Trip
import com.xinghan.xingtu.domain.model.TripStatistics
import com.xinghan.xingtu.domain.model.TripStatus

/**
 * Aggregates trips and items into the statistics model:
 * totals, completion rate, 7-day completion trend and per-category breakdown.
 */
class BuildStatisticsUseCase(
    private val calculateTripStatus: CalculateTripStatusUseCase,
    private val calculateProgress: CalculateTripProgressUseCase,
) {

    operator fun invoke(trips: List<Trip>, items: List<ChecklistItem>, todayEpochDay: Long): TripStatistics {
        val completedItems = items.count { it.isCompleted }

        val sevenDays = buildList {
            for (offset in 6 downTo 0) {
                val day = todayEpochDay - offset
                val dayStartMillis = day * MILLIS_PER_DAY
                val dayEndMillis = dayStartMillis + MILLIS_PER_DAY - 1
                val count = items.count {
                    val at = it.completedAt
                    at != null && at in dayStartMillis..dayEndMillis
                }
                add(DayCompletion(epochDay = day, count = count))
            }
        }

        val categoryStats = ChecklistCategory.DISPLAY_ORDER.map { category ->
            val categoryItems = items.filter { it.category == category }
            val done = categoryItems.count { it.isCompleted }
            CategoryStat(
                category = category,
                total = categoryItems.size,
                completed = done,
                percent = calculateProgress(done, categoryItems.size),
            )
        }

        return TripStatistics(
            tripCount = trips.size,
            finishedTripCount = trips.count {
                calculateTripStatus(it, todayEpochDay) == TripStatus.FINISHED
            },
            totalItemCount = items.size,
            completedItemCount = completedItems,
            completionPercent = calculateProgress(completedItems, items.size),
            sevenDaySeries = sevenDays,
            categoryStats = categoryStats,
        )
    }

    companion object {
        const val MILLIS_PER_DAY = 24L * 60L * 60L * 1000L
    }
}
