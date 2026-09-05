package com.xinghan.xingtu.domain.model

enum class ThemeMode(val rawName: String) {
    SYSTEM("system"),
    LIGHT("light"),
    DARK("dark"),
    ;

    companion object {
        fun fromRaw(raw: String?): ThemeMode =
            entries.firstOrNull { it.rawName == raw } ?: SYSTEM
    }
}
