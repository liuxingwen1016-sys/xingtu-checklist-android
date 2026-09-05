package com.xinghan.xingtu.domain.usecase

import kotlin.math.roundToInt

/**
 * Single source of truth for progress percentage.
 * total == 0 -> 0, otherwise round(completed * 100.0 / total).
 * Home, list, detail, stats and the widget must all use this function.
 */
class CalculateTripProgressUseCase {

    operator fun invoke(completed: Int, total: Int): Int =
        if (total <= 0) 0 else (completed * 100.0 / total).roundToInt()
}
