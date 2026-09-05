package com.xinghan.xingtu.domain.usecase

import com.xinghan.xingtu.domain.model.TripCard

/**
 * Builds the plain-text trip summary used by the system share sheet:
 * name, destination, dates and completion progress.
 */
class BuildShareSummaryUseCase(private val formatDateRange: (Long, Long) -> String) {

    operator fun invoke(card: TripCard): String = buildString {
        appendLine("【星途清单】行程分享")
        appendLine("行程：${card.trip.title}")
        appendLine("目的地：${card.trip.destination}")
        appendLine("日期：${formatDateRange(card.trip.startDate, card.trip.endDate)}")
        append("完成进度：${card.completedItems}/${card.totalItems}（${card.progressPercent}%）")
    }
}
