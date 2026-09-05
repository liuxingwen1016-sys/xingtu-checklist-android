package com.xinghan.xingtu.feature.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.xinghan.xingtu.AppContainer
import com.xinghan.xingtu.domain.model.TripStatistics
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

data class StatsUiState(
    val loading: Boolean = true,
    val failed: Boolean = false,
    val statistics: TripStatistics? = null,
)

class StatsViewModel(container: AppContainer) : ViewModel() {
    private val _uiState = MutableStateFlow(StatsUiState())
    val uiState: StateFlow<StatsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            container.tripRepository.observeStatistics()
                .catch { _uiState.value = StatsUiState(loading = false, failed = true) }
                .collect { _uiState.value = StatsUiState(loading = false, statistics = it) }
        }
    }

    companion object {
        fun provideFactory(container: AppContainer) = viewModelFactory {
            initializer { StatsViewModel(container) }
        }
    }
}
