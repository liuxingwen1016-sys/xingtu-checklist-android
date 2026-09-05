package com.xinghan.xingtu.feature.home

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.xinghan.xingtu.AppContainer
import com.xinghan.xingtu.R
import com.xinghan.xingtu.domain.model.PendingItem
import com.xinghan.xingtu.domain.model.TripCard
import com.xinghan.xingtu.domain.repository.TripRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.ExperimentalCoroutinesApi

data class HomeUiState(
    val loading: Boolean = true,
    val failed: Boolean = false,
    val tripCards: List<TripCard> = emptyList(),
    val selectedTripId: String? = null,
    val pendingItems: List<PendingItem> = emptyList(),
)

sealed interface HomeUiEffect {
    data class OpenTripDetail(val tripId: String) : HomeUiEffect
    data object OpenCreateTrip : HomeUiEffect
    data class ShowMessage(@param:StringRes val resId: Int) : HomeUiEffect
}

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModel(
    private val tripRepository: TripRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val _effects = Channel<HomeUiEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    private val reload = MutableSharedFlow<Unit>(replay = 1)
    private var pendingItemsByTrip: Map<String, List<PendingItem>> = emptyMap()

    init {
        reload.tryEmit(Unit)
        viewModelScope.launch {
            reload.flatMapLatest {
                tripRepository.observeDashboard()
                    .catch {
                        _uiState.value = _uiState.value.copy(loading = false, failed = true)
                    }
            }.collect { data ->
                pendingItemsByTrip = data.pendingItemsByTrip
                val previousSelection = _uiState.value.selectedTripId
                val selectedTripId = previousSelection
                    ?.takeIf { selected -> data.activeTrips.any { it.trip.id == selected } }
                    ?: data.activeTrips.firstOrNull()?.trip?.id
                _uiState.value = HomeUiState(
                    loading = false,
                    failed = false,
                    tripCards = data.activeTrips,
                    selectedTripId = selectedTripId,
                    pendingItems = selectedTripId?.let(pendingItemsByTrip::get).orEmpty(),
                )
            }
        }
    }

    fun retry() {
        _uiState.value = HomeUiState(loading = true)
        reload.tryEmit(Unit)
    }

    fun onHeroCardClicked(tripId: String) {
        if (_uiState.value.tripCards.any { it.trip.id == tripId }) {
            _effects.trySend(HomeUiEffect.OpenTripDetail(tripId))
        }
    }

    fun onHeroPageSelected(tripId: String) {
        if (_uiState.value.selectedTripId == tripId) return
        if (_uiState.value.tripCards.none { it.trip.id == tripId }) return
        _uiState.value = _uiState.value.copy(
            selectedTripId = tripId,
            pendingItems = pendingItemsByTrip[tripId].orEmpty(),
        )
    }

    fun onCreateTripClicked() {
        _effects.trySend(HomeUiEffect.OpenCreateTrip)
    }

    fun onPendingItemClicked(tripId: String) {
        _effects.trySend(HomeUiEffect.OpenTripDetail(tripId))
    }

    fun onToggleItemCompleted(itemId: String, completed: Boolean) {
        viewModelScope.launch {
            runCatching { tripRepository.setChecklistItemCompleted(itemId, completed) }
                .onFailure { _effects.trySend(HomeUiEffect.ShowMessage(R.string.error_generic)) }
        }
    }

    companion object {
        fun provideFactory(container: AppContainer) = viewModelFactory {
            initializer { HomeViewModel(container.tripRepository) }
        }
    }
}
