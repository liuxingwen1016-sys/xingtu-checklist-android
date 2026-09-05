package com.xinghan.xingtu.feature.settings

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.xinghan.xingtu.AppContainer
import com.xinghan.xingtu.R
import com.xinghan.xingtu.domain.model.ThemeMode
import com.xinghan.xingtu.domain.repository.SettingsRepository
import com.xinghan.xingtu.domain.repository.TripRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

data class SettingsUiState(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val hapticEnabled: Boolean = true,
    val notificationEnabled: Boolean = false,
    val resetting: Boolean = false,
)

sealed interface SettingsEffect {
    data class ThemeChanged(val mode: ThemeMode) : SettingsEffect
    data class ShowMessage(@param:StringRes val message: Int) : SettingsEffect
}

class SettingsViewModel(
    private val settings: SettingsRepository,
    private val trips: TripRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()
    private val _effects = Channel<SettingsEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    init {
        viewModelScope.launch {
            combine(
                settings.observeThemeMode(),
                settings.observeHapticEnabled(),
                settings.observeNotificationEnabled(),
            ) { theme, haptic, notification -> Triple(theme, haptic, notification) }
                .collect { (theme, haptic, notification) ->
                    _uiState.value = _uiState.value.copy(
                        themeMode = theme,
                        hapticEnabled = haptic,
                        notificationEnabled = notification,
                    )
                }
        }
    }

    fun setThemeMode(mode: ThemeMode) {
        if (mode == _uiState.value.themeMode) return
        viewModelScope.launch {
            settings.setThemeMode(mode)
            _effects.send(SettingsEffect.ThemeChanged(mode))
        }
    }

    fun setHapticEnabled(value: Boolean) {
        viewModelScope.launch { settings.setHapticEnabled(value) }
    }

    fun setNotificationEnabled(value: Boolean) {
        viewModelScope.launch { settings.setNotificationEnabled(value) }
    }

    fun resetDemoData() {
        _uiState.value = _uiState.value.copy(resetting = true)
        viewModelScope.launch {
            runCatching { trips.resetDemoData() }
                .onSuccess { _effects.send(SettingsEffect.ShowMessage(R.string.settings_reset_done)) }
                .onFailure { _effects.send(SettingsEffect.ShowMessage(R.string.settings_reset_failed)) }
            _uiState.value = _uiState.value.copy(resetting = false)
        }
    }

    companion object {
        fun provideFactory(container: AppContainer) = viewModelFactory {
            initializer { SettingsViewModel(container.settingsRepository, container.tripRepository) }
        }
    }
}
