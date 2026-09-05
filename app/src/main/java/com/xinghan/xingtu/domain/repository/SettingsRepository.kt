package com.xinghan.xingtu.domain.repository

import com.xinghan.xingtu.domain.model.ThemeMode
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    fun observeThemeMode(): Flow<ThemeMode>
    fun observeHapticEnabled(): Flow<Boolean>
    fun observeNotificationEnabled(): Flow<Boolean>
    fun isHapticEnabled(): Boolean
    suspend fun setThemeMode(mode: ThemeMode)
    suspend fun setHapticEnabled(enabled: Boolean)
    suspend fun setNotificationEnabled(enabled: Boolean)
    suspend fun getHasSeededDemoData(): Boolean
    suspend fun setHasSeededDemoData(value: Boolean)
    suspend fun getLastSelectedTripId(): String?
    suspend fun setLastSelectedTripId(tripId: String?)
}
