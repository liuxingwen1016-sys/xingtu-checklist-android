package com.xinghan.xingtu.feature.tripdetail

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.xinghan.xingtu.AppContainer
import com.xinghan.xingtu.R
import com.xinghan.xingtu.domain.model.TripCard
import com.xinghan.xingtu.domain.model.TripDetail
import com.xinghan.xingtu.domain.repository.TripRepository
import com.xinghan.xingtu.domain.usecase.CalculateTripProgressUseCase
import com.xinghan.xingtu.domain.usecase.CalculateTripStatusUseCase
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

data class TripDetailUiState(
    val loading: Boolean = true,
    val failed: Boolean = false,
    val detail: TripDetail? = null,
    val card: TripCard? = null,
)

sealed interface TripDetailEffect {
    data class ShowMessage(@param:StringRes val resId: Int) : TripDetailEffect
    data object TripDeleted : TripDetailEffect
}

class TripDetailViewModel(
    private val tripId: String,
    private val repository: TripRepository,
    private val status: CalculateTripStatusUseCase,
    private val progress: CalculateTripProgressUseCase,
    private val todayEpochDay: () -> Long,
) : ViewModel() {

    private val _uiState = MutableStateFlow(TripDetailUiState())
    val uiState: StateFlow<TripDetailUiState> = _uiState.asStateFlow()
    private val _effects = Channel<TripDetailEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    init {
        viewModelScope.launch {
            repository.observeTripDetail(tripId)
                .catch { _uiState.value = TripDetailUiState(loading = false, failed = true) }
                .collect { detail ->
                    val card = detail?.let {
                        val completed = it.items.count { item -> item.isCompleted }
                        TripCard(
                            trip = it.trip,
                            status = status(it.trip, todayEpochDay()),
                            totalItems = it.items.size,
                            completedItems = completed,
                            progressPercent = progress(completed, it.items.size),
                        )
                    }
                    _uiState.value = TripDetailUiState(loading = false, detail = detail, card = card)
                }
        }
    }

    fun toggleItem(itemId: String, completed: Boolean) = launchMutation {
        repository.setChecklistItemCompleted(itemId, completed)
    }

    fun deleteItem(itemId: String) = launchMutation(R.string.detail_item_deleted) {
        repository.deleteChecklistItem(itemId)
    }

    fun deleteTrip() {
        viewModelScope.launch {
            runCatching { repository.deleteTrip(tripId) }
                .onSuccess { _effects.send(TripDetailEffect.TripDeleted) }
                .onFailure { _effects.send(TripDetailEffect.ShowMessage(R.string.error_generic)) }
        }
    }

    private fun launchMutation(@StringRes successMessage: Int? = null, block: suspend () -> Unit) {
        viewModelScope.launch {
            runCatching { block() }
                .onSuccess { successMessage?.let { _effects.send(TripDetailEffect.ShowMessage(it)) } }
                .onFailure { _effects.send(TripDetailEffect.ShowMessage(R.string.error_generic)) }
        }
    }

    companion object {
        fun provideFactory(container: AppContainer, tripId: String) = viewModelFactory {
            initializer {
                TripDetailViewModel(
                    tripId = tripId,
                    repository = container.tripRepository,
                    status = container.calculateTripStatus,
                    progress = container.calculateTripProgress,
                    todayEpochDay = container.dateProvider::todayEpochDay,
                )
            }
        }
    }
}
