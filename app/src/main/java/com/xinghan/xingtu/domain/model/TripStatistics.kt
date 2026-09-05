package com.xinghan.xingtu.domain.model

/** Aggregated statistics for the stats page. */
data class TripStatistics(
    val tripCount: Int,
    val finishedTripCount: Int,
    val totalItemCount: Int,
    val completedItemCount: Int,
    val completionPercent: Int,
    /** Oldest first, exactly 7 entries ending today. */
    val sevenDaySeries: List<DayCompletion>,
    /** Fixed category order, includes categories without items. */
    val categoryStats: List<CategoryStat>,
)

/** Number of items completed on a given day. */
data class DayCompletion(
    val epochDay: Long,
    val count: Int,
)

/** Completion breakdown for one category. */
data class CategoryStat(
    val category: ChecklistCategory,
    val total: Int,
    val completed: Int,
    val percent: Int,
)
