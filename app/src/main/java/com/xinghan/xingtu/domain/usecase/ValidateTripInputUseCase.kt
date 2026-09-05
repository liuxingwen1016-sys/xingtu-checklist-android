package com.xinghan.xingtu.domain.usecase

/**
 * Field-level validation errors, mapped to localized messages in the UI.
 */
enum class FieldError { NONE, EMPTY, TOO_LONG }

enum class DateError { NONE, END_BEFORE_START }

data class TripValidationResult(
    val titleError: FieldError = FieldError.NONE,
    val destinationError: FieldError = FieldError.NONE,
    val dateError: DateError = DateError.NONE,
) {
    val isValid: Boolean
        get() = titleError == FieldError.NONE &&
            destinationError == FieldError.NONE &&
            dateError == DateError.NONE
}

/**
 * Trip form rules:
 * - title/destination required, 1..30 chars;
 * - end date must not be earlier than the start date.
 */
class ValidateTripInputUseCase {

    operator fun invoke(
        title: String,
        destination: String,
        startDate: Long,
        endDate: Long,
    ): TripValidationResult = TripValidationResult(
        titleError = validateTextField(title.trim(), MAX_TRIP_FIELD),
        destinationError = validateTextField(destination.trim(), MAX_TRIP_FIELD),
        dateError = if (endDate < startDate) DateError.END_BEFORE_START else DateError.NONE,
    )

    private fun validateTextField(value: String, max: Int): FieldError = when {
        value.isEmpty() -> FieldError.EMPTY
        value.length > max -> FieldError.TOO_LONG
        else -> FieldError.NONE
    }

    companion object {
        const val MAX_TRIP_FIELD = 30
    }
}
