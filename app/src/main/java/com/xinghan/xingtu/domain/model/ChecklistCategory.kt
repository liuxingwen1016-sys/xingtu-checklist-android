package com.xinghan.xingtu.domain.model

/**
 * Checklist categories with a fixed display order.
 * Storage uses the stable [rawName]; UI maps it to Chinese labels via resources.
 */
enum class ChecklistCategory(val rawName: String) {
    DOCUMENT("document"),
    DIGITAL("digital"),
    CLOTHING("clothing"),
    WORK("work"),
    OTHER("other"),
    ;

    companion object {
        val DISPLAY_ORDER: List<ChecklistCategory> =
            listOf(DOCUMENT, DIGITAL, CLOTHING, WORK, OTHER)

        fun fromRaw(raw: String): ChecklistCategory =
            entries.firstOrNull { it.rawName == raw } ?: OTHER
    }
}
