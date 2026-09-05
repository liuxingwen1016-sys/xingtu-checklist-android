package com.xinghan.xingtu.data.seed

import com.xinghan.xingtu.domain.model.ChecklistCategory
import com.xinghan.xingtu.domain.model.ChecklistPriority
import com.xinghan.xingtu.domain.model.TripInput
import com.xinghan.xingtu.domain.model.TripThemes

/** A fully specified demo trip with fixed IDs and dynamic dates. */
data class DemoTripSeed(
    val tripId: String,
    val input: TripInput,
    val items: List<DemoItemSeed>,
)

data class DemoItemSeed(
    val itemId: String,
    val title: String,
    val category: ChecklistCategory,
    val priority: ChecklistPriority,
    val isCompleted: Boolean,
    val completedAt: Long?,
    val sortOrder: Int,
)

/**
 * Builds the fixed demo dataset:
 * - Shenzhen tech exchange trip: D+7..D+9, 10 items, 6 completed (60%);
 * - Hangzhou weekend stroll: D-14..D-12, 8 items, all completed (100%).
 *
 * IDs are fixed constants (used by tests and widget deep links); dates are
 * computed from "today" so the demo never goes stale.
 */
class DemoDataFactory {

    fun build(todayEpochDay: Long, nowMillis: Long): List<DemoTripSeed> = listOf(
        buildShenzhen(todayEpochDay, nowMillis),
        buildHangzhou(todayEpochDay, nowMillis),
    )

    private fun buildShenzhen(today: Long, now: Long): DemoTripSeed {
        val templates = ChecklistTemplates()
        val completedTitles = setOf(
            "笔记本电脑与充电器",
            "身份证与证件照",
            "名片与工作证件",
            "正装与换洗衣物",
            "洗漱用品",
            "轻便背包",
        )
        // Spread completion stamps over the last 7 days for a lively trend chart.
        val completionOffsets = listOf(6L, 4L, 2L, 2L, 1L, 0L)
        var completionIndex = 0
        val items = templates.business().mapIndexed { index, template ->
            val completed = template.title in completedTitles
            val completedAt = if (completed) {
                val day = today - completionOffsets[completionIndex++]
                day * MILLIS_PER_DAY + COMPLETED_HOUR_MILLIS
            } else {
                null
            }
            DemoItemSeed(
                itemId = "demo-item-sz-${index + 1}",
                title = template.title,
                category = template.category,
                priority = template.priority,
                isCompleted = completed,
                completedAt = completedAt,
                sortOrder = index,
            )
        }
        return DemoTripSeed(
            tripId = DEMO_TRIP_SHENZHEN_ID,
            input = TripInput(
                title = "深圳技术交流行程",
                destination = "深圳",
                startDate = today + 7,
                endDate = today + 9,
                themeKey = TripThemes.DEFAULT,
                note = "客户现场技术交流，注意演示环境与资料版本。",
            ),
            items = items,
        )
    }

    private fun buildHangzhou(today: Long, now: Long): DemoTripSeed {
        val templates = ChecklistTemplates()
        // Trip was D-14..D-12; mark items completed during the trip.
        val completionDays = listOf(14L, 14L, 13L, 13L, 13L, 12L, 12L, 12L)
        val items = templates.weekend().mapIndexed { index, template ->
            val day = today - completionDays[index]
            DemoItemSeed(
                itemId = "demo-item-hz-${index + 1}",
                title = template.title,
                category = template.category,
                priority = template.priority,
                isCompleted = true,
                completedAt = day * MILLIS_PER_DAY + COMPLETED_HOUR_MILLIS,
                sortOrder = index,
            )
        }
        return DemoTripSeed(
            tripId = DEMO_TRIP_HANGZHOU_ID,
            input = TripInput(
                title = "杭州周末漫游",
                destination = "杭州",
                startDate = today - 14,
                endDate = today - 12,
                themeKey = "teal",
                note = "周末城市漫游，轻装出行。",
            ),
            items = items,
        )
    }

    companion object {
        const val DEMO_TRIP_SHENZHEN_ID = "demo-trip-shenzhen"
        const val DEMO_TRIP_HANGZHOU_ID = "demo-trip-hangzhou"
        const val MILLIS_PER_DAY = 24L * 60L * 60L * 1000L
        private const val COMPLETED_HOUR_MILLIS = 10L * 60L * 60L * 1000L
    }
}
