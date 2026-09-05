package com.xinghan.xingtu.domain.usecase

/**
 * Checklist item form rules:
 * - title required, 1..40 chars;
 * - note optional, at most 200 chars.
 */
class ValidateChecklistItemUseCase {

    operator fun invoke(title: String, note: String): Pair<FieldError, FieldError> =
        validateTextField(title.trim(), MAX_ITEM_TITLE) to
            validateNote(note.trim(), MAX_ITEM_NOTE)

    private fun validateTextField(value: String, max: Int): FieldError = when {
        value.isEmpty() -> FieldError.EMPTY
        value.length > max -> FieldError.TOO_LONG
        else -> FieldError.NONE
    }

    private fun validateNote(value: String, max: Int): FieldError =
        if (value.length > max) FieldError.TOO_LONG else FieldError.NONE

    companion object {
        const val MAX_ITEM_TITLE = 40
        const val MAX_ITEM_NOTE = 200
    }
}
