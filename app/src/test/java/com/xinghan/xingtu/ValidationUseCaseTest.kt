package com.xinghan.xingtu

import com.xinghan.xingtu.domain.usecase.DateError
import com.xinghan.xingtu.domain.usecase.FieldError
import com.xinghan.xingtu.domain.usecase.ValidateChecklistItemUseCase
import com.xinghan.xingtu.domain.usecase.ValidateTripInputUseCase
import org.junit.Assert.assertEquals
import org.junit.Test

class ValidateTripInputUseCaseTest {

    private val useCase = ValidateTripInputUseCase()

    @Test
    fun `valid input passes`() {
        val result = useCase("深圳行程", "深圳", startDate = 10, endDate = 12)
        assertEquals(true, result.isValid)
    }

    @Test
    fun `blank title is empty error`() {
        val result = useCase("   ", "深圳", 10, 12)
        assertEquals(FieldError.EMPTY, result.titleError)
    }

    @Test
    fun `title over 30 chars is too long`() {
        val result = useCase("a".repeat(31), "深圳", 10, 12)
        assertEquals(FieldError.TOO_LONG, result.titleError)
    }

    @Test
    fun `title of exactly 30 chars is valid`() {
        val result = useCase("a".repeat(30), "深圳", 10, 12)
        assertEquals(FieldError.NONE, result.titleError)
    }

    @Test
    fun `blank destination is empty error`() {
        val result = useCase("行程", "", 10, 12)
        assertEquals(FieldError.EMPTY, result.destinationError)
    }

    @Test
    fun `destination over 30 chars is too long`() {
        val result = useCase("行程", "b".repeat(31), 10, 12)
        assertEquals(FieldError.TOO_LONG, result.destinationError)
    }

    @Test
    fun `end before start is date error`() {
        val result = useCase("行程", "深圳", startDate = 10, endDate = 9)
        assertEquals(DateError.END_BEFORE_START, result.dateError)
    }

    @Test
    fun `end equals start is valid`() {
        val result = useCase("行程", "深圳", startDate = 10, endDate = 10)
        assertEquals(DateError.NONE, result.dateError)
    }

    @Test
    fun `multiple errors are reported together`() {
        val result = useCase("", "c".repeat(31), 10, 9)
        assertEquals(FieldError.EMPTY, result.titleError)
        assertEquals(FieldError.TOO_LONG, result.destinationError)
        assertEquals(DateError.END_BEFORE_START, result.dateError)
    }
}

class ValidateChecklistItemUseCaseTest {

    private val useCase = ValidateChecklistItemUseCase()

    @Test
    fun `valid item passes`() {
        val (titleError, noteError) = useCase("携带充电宝", "记住带 65W 的")
        assertEquals(FieldError.NONE, titleError)
        assertEquals(FieldError.NONE, noteError)
    }

    @Test
    fun `blank title is empty error`() {
        val (titleError, _) = useCase("  ", "")
        assertEquals(FieldError.EMPTY, titleError)
    }

    @Test
    fun `title over 40 chars is too long`() {
        val (titleError, _) = useCase("a".repeat(41), "")
        assertEquals(FieldError.TOO_LONG, titleError)
    }

    @Test
    fun `title of exactly 40 chars is valid`() {
        val (titleError, _) = useCase("a".repeat(40), "")
        assertEquals(FieldError.NONE, titleError)
    }

    @Test
    fun `note over 200 chars is too long`() {
        val (_, noteError) = useCase("事项", "n".repeat(201))
        assertEquals(FieldError.TOO_LONG, noteError)
    }

    @Test
    fun `note of exactly 200 chars is valid`() {
        val (_, noteError) = useCase("事项", "n".repeat(200))
        assertEquals(FieldError.NONE, noteError)
    }
}
