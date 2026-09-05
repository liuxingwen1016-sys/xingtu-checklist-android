package com.xinghan.xingtu.domain.model

/**
 * A trip as stored in the database. Dates are Epoch Day values so that
 * no timezone can shift them.
 */
data class Trip(
    val id: String,
    val title: String,
    val destination: String,
    val startDate: Long,
    val endDate: Long,
    val themeKey: String,
    val note: String?,
    val createdAt: Long,
    val updatedAt: Long,
)
