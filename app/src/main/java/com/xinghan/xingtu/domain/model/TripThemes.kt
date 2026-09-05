package com.xinghan.xingtu.domain.model

/**
 * Preset trip accent themes. Keys are stored in the database and mapped
 * to color resources in the UI layer.
 */
object TripThemes {
    const val DEFAULT = "indigo"
    val ALL: List<String> = listOf("indigo", "blue", "teal", "amber", "coral", "violet")

    fun isSupported(key: String): Boolean = key in ALL
}
