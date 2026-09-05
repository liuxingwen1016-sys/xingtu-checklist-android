package com.xinghan.xingtu.feature.tripedit

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.xinghan.xingtu.AppContainer
import com.xinghan.xingtu.R
import com.xinghan.xingtu.domain.model.TripInput
import com.xinghan.xingtu.domain.model.TripTemplate
import com.xinghan.xingtu.domain.model.TripThemes
import com.xinghan.xingtu.domain.repository.TripRepository
import com.xinghan.xingtu.domain.usecase.DateError
import com.xinghan.xingtu.domain.usecase.FieldError
import com.xinghan.xingtu.domain.usecase.ValidateTripInputUseCase
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

data class TripEditUiState(
    val loading: Boolean = false,
    val saving: Boolean = false,
    val isEditing: Boolean = false,
    val title: String = "",
    val destination: String = "",
    val startDate: Long = 0,
    val endDate: Long = 0,
    val themeKey: String = TripThemes.DEFAULT,
    val template: TripTemplate = TripTemplate.BUSINESS,
    val note: String = "",
    val titleError: FieldError = FieldError.NONE,
    val destinationError: FieldError = FieldError.NONE,
    val dateError: DateError = DateError.NONE,
)

sealed interface TripEditEffect {
    data class Saved(val tripId: String, @param:StringRes val message: Int) : TripEditEffect
    data class ShowMessage(@param:StringRes val message: Int) : TripEditEffect
}

class TripEditViewModel(
    private val tripId: String?,
    private val repository: TripRepository,
    private val validate: ValidateTripInputUseCase,
    today: Long,
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        TripEditUiState(
            loading = tripId != null,
            isEditing = tripId != null,
            startDate = today + 7,
            endDate = today + 9,
        )
    )
    val uiState: StateFlow<TripEditUiState> = _uiState.asStateFlow()
    private val _effects = Channel<TripEditEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    init {
        if (tripId != null) loadTrip(tripId)
    }

    private fun loadTrip(id: String) {
        viewModelScope.launch {
            runCatching { repository.observeTripDetail(id).first() }
                .onSuccess { detail ->
                    val trip = detail?.trip
                    if (trip == null) {
                        _uiState.value = _uiState.value.copy(loading = false)
                        _effects.send(TripEditEffect.ShowMessage(R.string.detail_not_found_title))
                    } else {
                        _uiState.value = _uiState.value.copy(
                            loading = false,
                            title = trip.title,
                            destination = trip.destination,
                            startDate = trip.startDate,
                            endDate = trip.endDate,
                            themeKey = trip.themeKey,
                            note = trip.note.orEmpty(),
                        )
                    }
                }
                .onFailure {
                    _uiState.value = _uiState.value.copy(loading = false)
                    _effects.send(TripEditEffect.ShowMessage(R.string.error_load_failed))
                }
        }
    }

    fun updateTitle(value: String) = update { copy(title = value, titleError = FieldError.NONE) }
    fun updateDestination(value: String) = update { copy(destination = value, destinationError = FieldError.NONE) }
    fun updateStartDate(value: Long) = update { copy(startDate = value, dateError = DateError.NONE) }
    fun updateEndDate(value: Long) = update { copy(endDate = value, dateError = DateError.NONE) }
    fun updateTheme(value: String) = update { copy(themeKey = value) }
    fun updateTemplate(value: TripTemplate) = update { copy(template = value) }
    fun updateNote(value: String) = update { copy(note = value) }

    fun save() {
        val state = _uiState.value
        val result = validate(state.title, state.destination, state.startDate, state.endDate)
        if (!result.isValid) {
            _uiState.value = state.copy(
                titleError = result.titleError,
                destinationError = result.destinationError,
                dateError = result.dateError,
            )
            return
        }
        _uiState.value = state.copy(saving = true)
        viewModelScope.launch {
            val input = TripInput(
                title = state.title,
                destination = state.destination,
                startDate = state.startDate,
                endDate = state.endDate,
                themeKey = state.themeKey,
                note = state.note,
            )
            runCatching {
                if (tripId == null) repository.createTrip(input, state.template)
                else {
                    repository.updateTrip(tripId, input)
                    tripId
                }
            }.onSuccess { savedId ->
                _uiState.value = _uiState.value.copy(saving = false)
                _effects.send(
                    TripEditEffect.Saved(
                        savedId,
                        if (tripId == null) R.string.trip_edit_created else R.string.trip_edit_updated,
                    )
                )
            }.onFailure {
                _uiState.value = _uiState.value.copy(saving = false)
                _effects.send(TripEditEffect.ShowMessage(R.string.error_save_failed))
            }
        }
    }

    private fun update(block: TripEditUiState.() -> TripEditUiState) {
        _uiState.value = _uiState.value.block()
    }

    companion object {
        fun provideFactory(container: AppContainer, tripId: String?) = viewModelFactory {
            initializer {
                TripEditViewModel(
                    tripId = tripId,
                    repository = container.tripRepository,
                    validate = container.validateTripInput,
                    today = container.dateProvider.todayEpochDay(),
                )
            }
        }
    }
}
