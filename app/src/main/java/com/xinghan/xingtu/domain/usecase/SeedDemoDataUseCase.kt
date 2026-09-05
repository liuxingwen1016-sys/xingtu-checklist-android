package com.xinghan.xingtu.domain.usecase

import com.xinghan.xingtu.domain.repository.TripRepository

/**
 * Resets the database to the fixed demo dataset (dynamic dates, fixed IDs).
 */
class SeedDemoDataUseCase(private val repository: TripRepository) {

    suspend operator fun invoke() = repository.resetDemoData()
}
