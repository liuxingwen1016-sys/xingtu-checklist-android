package com.xinghan.xingtu

import com.xinghan.xingtu.domain.usecase.CalculateTripProgressUseCase
import org.junit.Assert.assertEquals
import org.junit.Test

class CalculateTripProgressUseCaseTest {

    private val useCase = CalculateTripProgressUseCase()

    @Test
    fun `zero items is zero percent`() {
        assertEquals(0, useCase(completed = 0, total = 0))
    }

    @Test
    fun `partial completion rounds`() {
        assertEquals(60, useCase(completed = 6, total = 10))
        assertEquals(33, useCase(completed = 1, total = 3))
        assertEquals(67, useCase(completed = 2, total = 3))
    }

    @Test
    fun `all completed is one hundred`() {
        assertEquals(100, useCase(completed = 10, total = 10))
    }

    @Test
    fun `invalid total does not crash`() {
        assertEquals(0, useCase(completed = 3, total = -1))
    }
}
