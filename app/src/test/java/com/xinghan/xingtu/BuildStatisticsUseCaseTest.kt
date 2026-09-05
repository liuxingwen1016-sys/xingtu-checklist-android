package com.xinghan.xingtu

import com.xinghan.xingtu.TestFixtures.item
import com.xinghan.xingtu.TestFixtures.trip
import com.xinghan.xingtu.domain.model.ChecklistCategory
import com.xinghan.xingtu.domain.usecase.BuildStatisticsUseCase
import com.xinghan.xingtu.domain.usecase.CalculateTripProgressUseCase
import com.xinghan.xingtu.domain.usecase.CalculateTripStatusUseCase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BuildStatisticsUseCaseTest {

    private val useCase = BuildStatisticsUseCase(
        CalculateTripStatusUseCase(),
        CalculateTripProgressUseCase(),
    )

    private val today = 100L
    private val millisPerDay = BuildStatisticsUseCase.MILLIS_PER_DAY

    @Test
    fun `empty data yields zeros and seven zero bars`() {
        val stats = useCase(emptyList(), emptyList(), today)
        assertEquals(0, stats.tripCount)
        assertEquals(0, stats.totalItemCount)
        assertEquals(0, stats.completionPercent)
        assertEquals(7, stats.sevenDaySeries.size)
        assertTrue(stats.sevenDaySeries.all { it.count == 0 })
        assertEquals(5, stats.categoryStats.size)
    }

    @Test
    fun `series has seven days ending today`() {
        val stats = useCase(emptyList(), emptyList(), today)
        assertEquals(today - 6, stats.sevenDaySeries.first().epochDay)
        assertEquals(today, stats.sevenDaySeries.last().epochDay)
    }

    @Test
    fun `completions land on their day and undo removes the count`() {
        val doneToday = item(
            id = "i1", tripId = "t1", isCompleted = true,
            completedAt = today * millisPerDay + 100,
        )
        val doneThreeDaysAgo = item(
            id = "i2", tripId = "t1", isCompleted = true,
            completedAt = (today - 3) * millisPerDay + 100,
        )
        val undone = item(
            id = "i3", tripId = "t1", isCompleted = false, completedAt = null,
        )
        val stats = useCase(listOf(trip(id = "t1")), listOf(doneToday, doneThreeDaysAgo, undone), today)

        assertEquals(2, stats.completedItemCount)
        assertEquals(3, stats.totalItemCount)
        assertEquals(1, stats.sevenDaySeries.last().count)
        assertEquals(1, stats.sevenDaySeries.first { it.epochDay == today - 3 }.count)
        // Undo path: completed=false and completedAt cleared contributes nothing.
        assertEquals(67, stats.completionPercent)
    }

    @Test
    fun `completions outside the window are excluded from the trend`() {
        val old = item(
            id = "i1", tripId = "t1", isCompleted = true,
            completedAt = (today - 10) * millisPerDay + 100,
        )
        val stats = useCase(listOf(trip(id = "t1")), listOf(old), today)
        assertEquals(1, stats.completedItemCount)
        assertEquals(0, stats.sevenDaySeries.sumOf { it.count })
    }

    @Test
    fun `category stats follow the fixed display order and include empties`() {
        val items = listOf(
            item(id = "a", category = ChecklistCategory.WORK, isCompleted = true, completedAt = today * millisPerDay),
            item(id = "b", category = ChecklistCategory.WORK, isCompleted = false),
            item(id = "c", category = ChecklistCategory.DIGITAL, isCompleted = true, completedAt = today * millisPerDay),
        )
        val stats = useCase(listOf(trip(id = "t1")), items, today)
        val order = stats.categoryStats.map { it.category }
        assertEquals(ChecklistCategory.DISPLAY_ORDER, order)

        val work = stats.categoryStats.first { it.category == ChecklistCategory.WORK }
        assertEquals(2, work.total)
        assertEquals(1, work.completed)
        assertEquals(50, work.percent)

        val document = stats.categoryStats.first { it.category == ChecklistCategory.DOCUMENT }
        assertEquals(0, document.total)
        assertEquals(0, document.percent)
    }

    @Test
    fun `finished trip count is derived from dates`() {
        val finished = trip(id = "f", startDate = today - 10, endDate = today - 5)
        val active = trip(id = "a", startDate = today + 1, endDate = today + 2)
        val stats = useCase(listOf(finished, active), emptyList(), today)
        assertEquals(2, stats.tripCount)
        assertEquals(1, stats.finishedTripCount)
    }
}
