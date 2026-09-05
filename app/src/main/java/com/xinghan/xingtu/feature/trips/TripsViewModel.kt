package com.xinghan.xingtu.feature.trips

import androidx.annotation.StringRes
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.xinghan.xingtu.AppContainer
import com.xinghan.xingtu.R
import com.xinghan.xingtu.domain.model.TripCard
import com.xinghan.xingtu.domain.repository.TripRepository
import com.xinghan.xingtu.domain.usecase.FilterAndSortTripsUseCase
import com.xinghan.xingtu.domain.usecase.TripFilter
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

data class TripsUiState(
    val loading: Boolean = true,
    val failed: Boolean = false,
    val query: String = "",
    val filter: TripFilter = TripFilter.ALL,
    val tripCards: List<TripCard> = emptyList(),
    val selectedTripId: String? = null,
    val isTwoPane: Boolean = false,
)

sealed interface TripsUiEffect {
    data class OpenTripDetail(val tripId: String) : TripsUiEffect
    data object OpenCreateTrip : TripsUiEffect
    data class ShowMessage(@param:StringRes val resId: Int) : TripsUiEffect
}

class TripsViewModel(
    private val tripRepository: TripRepository,
    private val filterAndSortTrips: FilterAndSortTripsUseCase,
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        TripsUiState(
            query = savedStateHandle.get<String>(KEY_QUERY).orEmpty(),
            filter = TripFilter.fromRaw(savedStateHandle.get<String>(KEY_FILTER)),
            selectedTripId = savedStateHandle.get<String>(KEY_SELECTED_TRIP),
        )
    )
    val uiState: StateFlow<TripsUiState> = _uiState.asStateFlow()

    private val _effects = Channel<TripsUiEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    private val queryInput = MutableStateFlow(_uiState.value.query)
    private val filterInput = MutableStateFlow(_uiState.value.filter)

    init {
        observeTripCards()
    }

    @OptIn(FlowPreview::class)
    private fun observeTripCards() {
        combine(
            tripRepository.observeTripCards().catch { emit(emptyList()) },
            queryInput.debounce(SEARCH_DEBOUNCE_MS),
            filterInput,
        ) { cards, query, filter ->
            Triple(cards, query, filter)
        }
            .onEach { (cards, query, filter) ->
                _uiState.value = _uiState.value.copy(
                    loading = false,
                    tripCards = filterAndSortTrips(cards, query, filter),
                )
            }
            .launchIn(viewModelScope)
    }

    fun onQueryChanged(query: String) {
        _uiState.value = _uiState.value.copy(query = query)
        savedStateHandle[KEY_QUERY] = query
        queryInput.value = query
    }

    fun onFilterChanged(filter: TripFilter) {
        _uiState.value = _uiState.value.copy(filter = filter)
        savedStateHandle[KEY_FILTER] = filter.rawName
        filterInput.value = filter
    }

    fun onTripClicked(tripId: String, isTwoPane: Boolean) {
        if (isTwoPane) {
            _uiState.value = _uiState.value.copy(selectedTripId = tripId)
            savedStateHandle[KEY_SELECTED_TRIP] = tripId
        } else {
            _effects.trySend(TripsUiEffect.OpenTripDetail(tripId))
        }
    }

    /** Clears the two-pane selection (detail pane closed). */
    fun onDetailPaneCleared() {
        _uiState.value = _uiState.value.copy(selectedTripId = null)
        savedStateHandle[KEY_SELECTED_TRIP] = null
    }

    fun onTwoPaneChanged(isTwoPane: Boolean) {
        _uiState.value = _uiState.value.copy(isTwoPane = isTwoPane)
    }

    fun onCreateTripClicked() {
        _effects.trySend(TripsUiEffect.OpenCreateTrip)
    }

    fun onDeletedTripRemoved(deletedTripId: String) {
        if (_uiState.value.selectedTripId == deletedTripId) {
            onDetailPaneCleared()
        }
    }

    companion object {
        private const val KEY_QUERY = "trips_query"
        private const val KEY_FILTER = "trips_filter"
        private const val KEY_SELECTED_TRIP = "trips_selected_trip"
        private const val SEARCH_DEBOUNCE_MS = 250L

        fun provideFactory(container: AppContainer) = viewModelFactory {
            initializer {
                TripsViewModel(
                    tripRepository = container.tripRepository,
                    filterAndSortTrips = container.filterAndSortTrips,
                    savedStateHandle = createSavedStateHandle(),
                )
            }
        }
    }
}
