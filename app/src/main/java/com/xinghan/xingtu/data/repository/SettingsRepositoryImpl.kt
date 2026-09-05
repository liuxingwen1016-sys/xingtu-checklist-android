package com.xinghan.xingtu.data.repository

import android.content.Context
import com.xinghan.xingtu.domain.model.ThemeMode
import com.xinghan.xingtu.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * SharedPreferences-backed settings storage.
 * File name: xingtu_preferences. Keys are stable lowercase snake_case strings.
 */
class SettingsRepositoryImpl(context: Context) : SettingsRepository {

    private val prefs = context.applicationContext
        .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val themeModeFlow = MutableStateFlow(readThemeMode())
    private val hapticFlow = MutableStateFlow(prefs.getBoolean(KEY_HAPTIC, DEFAULT_HAPTIC))
    private val notificationFlow =
        MutableStateFlow(prefs.getBoolean(KEY_NOTIFICATION, DEFAULT_NOTIFICATION))

    override fun observeThemeMode(): StateFlow<ThemeMode> = themeModeFlow.asStateFlow()

    override fun observeHapticEnabled(): StateFlow<Boolean> = hapticFlow.asStateFlow()

    override fun observeNotificationEnabled(): StateFlow<Boolean> = notificationFlow.asStateFlow()

    override fun isHapticEnabled(): Boolean = prefs.getBoolean(KEY_HAPTIC, DEFAULT_HAPTIC)

    override suspend fun setThemeMode(mode: ThemeMode) {
        prefs.edit().putString(KEY_THEME, mode.rawName).apply()
        themeModeFlow.value = mode
    }

    override suspend fun setHapticEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_HAPTIC, enabled).apply()
        hapticFlow.value = enabled
    }

    override suspend fun setNotificationEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_NOTIFICATION, enabled).apply()
        notificationFlow.value = enabled
    }

    override suspend fun getHasSeededDemoData(): Boolean =
        prefs.getBoolean(KEY_SEEDED, false)

    override suspend fun setHasSeededDemoData(value: Boolean) {
        prefs.edit().putBoolean(KEY_SEEDED, value).apply()
    }

    override suspend fun getLastSelectedTripId(): String? =
        prefs.getString(KEY_LAST_TRIP, null)

    override suspend fun setLastSelectedTripId(tripId: String?) {
        if (tripId == null) {
            prefs.edit().remove(KEY_LAST_TRIP).apply()
        } else {
            prefs.edit().putString(KEY_LAST_TRIP, tripId).apply()
        }
    }

    private fun readThemeMode(): ThemeMode = ThemeMode.fromRaw(prefs.getString(KEY_THEME, null))

    companion object {
        const val PREFS_NAME = "xingtu_preferences"
        private const val KEY_THEME = "theme_mode"
        private const val KEY_HAPTIC = "haptic_enabled"
        private const val KEY_NOTIFICATION = "notification_enabled"
        private const val KEY_SEEDED = "has_seeded_demo_data"
        private const val KEY_LAST_TRIP = "last_selected_trip_id"
        private const val DEFAULT_HAPTIC = true
        private const val DEFAULT_NOTIFICATION = false
    }
}
