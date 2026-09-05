package com.xinghan.xingtu.domain.model

enum class ChecklistPriority(val rawValue: Int) {
    NORMAL(0),
    IMPORTANT(1),
    ;

    val isImportant: Boolean
        get() = this == IMPORTANT

    companion object {
        fun fromRaw(value: Int): ChecklistPriority =
            if (value == 1) IMPORTANT else NORMAL
    }
}
