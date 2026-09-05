package com.xinghan.xingtu.feature.itemedit

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.xinghan.xingtu.AppContainer
import com.xinghan.xingtu.R
import com.xinghan.xingtu.domain.model.ChecklistCategory
import com.xinghan.xingtu.domain.model.ChecklistItemInput
import com.xinghan.xingtu.domain.model.ChecklistPriority
import com.xinghan.xingtu.domain.repository.TripRepository
import com.xinghan.xingtu.domain.usecase.FieldError
import com.xinghan.xingtu.domain.usecase.ValidateChecklistItemUseCase
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

data class ItemEditUiState(
    val loading: Boolean = false,
    val saving: Boolean = false,
    val isEditing: Boolean = false,
    val title: String = "",
    val note: String = "",
    val category: ChecklistCategory = ChecklistCategory.OTHER,
    val important: Boolean = false,
    val titleError: FieldError = FieldError.NONE,
    val noteError: FieldError = FieldError.NONE,
)

sealed interface ItemEditEffect {
    data class Done(@param:StringRes val message: Int) : ItemEditEffect
    data class ShowMessage(@param:StringRes val message: Int) : ItemEditEffect
}

class ItemEditViewModel(
    private val tripId: String,
    private val itemId: String?,
    private val repository: TripRepository,
    private val validate: ValidateChecklistItemUseCase,
) : ViewModel() {
    private val _uiState = MutableStateFlow(ItemEditUiState(loading = itemId != null, isEditing = itemId != null))
    val uiState: StateFlow<ItemEditUiState> = _uiState.asStateFlow()
    private val _effects = Channel<ItemEditEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    init {
        if (itemId != null) load(itemId)
    }

    private fun load(id: String) {
        viewModelScope.launch {
            runCatching { repository.observeTripDetail(tripId).first()?.items?.firstOrNull { it.id == id } }
                .onSuccess { item ->
                    _uiState.value = if (item == null) {
                        _effects.send(ItemEditEffect.ShowMessage(R.string.detail_not_found_title))
                        _uiState.value.copy(loading = false)
                    } else {
                        _uiState.value.copy(
                            loading = false,
                            title = item.title,
                            note = item.note.orEmpty(),
                            category = item.category,
                            important = item.priority.isImportant,
                        )
                    }
                }
                .onFailure {
                    _uiState.value = _uiState.value.copy(loading = false)
                    _effects.send(ItemEditEffect.ShowMessage(R.string.error_load_failed))
                }
        }
    }

    fun updateTitle(value: String) = update { copy(title = value, titleError = FieldError.NONE) }
    fun updateNote(value: String) = update { copy(note = value, noteError = FieldError.NONE) }
    fun updateCategory(value: ChecklistCategory) = update { copy(category = value) }
    fun updateImportant(value: Boolean) = update { copy(important = value) }

    fun save() {
        val state = _uiState.value
        val (titleError, noteError) = validate(state.title, state.note)
        if (titleError != FieldError.NONE || noteError != FieldError.NONE) {
            _uiState.value = state.copy(titleError = titleError, noteError = noteError)
            return
        }
        _uiState.value = state.copy(saving = true)
        viewModelScope.launch {
            val input = ChecklistItemInput(
                itemId = itemId,
                tripId = tripId,
                title = state.title,
                category = state.category,
                priority = if (state.important) ChecklistPriority.IMPORTANT else ChecklistPriority.NORMAL,
                note = state.note,
            )
            runCatching { repository.saveChecklistItem(input) }
                .onSuccess { _effects.send(ItemEditEffect.Done(R.string.item_edit_saved)) }
                .onFailure {
                    _uiState.value = _uiState.value.copy(saving = false)
                    _effects.send(ItemEditEffect.ShowMessage(R.string.error_save_failed))
                }
        }
    }

    fun delete() {
        val id = itemId ?: return
        viewModelScope.launch {
            runCatching { repository.deleteChecklistItem(id) }
                .onSuccess { _effects.send(ItemEditEffect.Done(R.string.item_edit_deleted)) }
                .onFailure { _effects.send(ItemEditEffect.ShowMessage(R.string.error_generic)) }
        }
    }

    private fun update(block: ItemEditUiState.() -> ItemEditUiState) {
        _uiState.value = _uiState.value.block()
    }

    companion object {
        fun provideFactory(container: AppContainer, tripId: String, itemId: String?) = viewModelFactory {
            initializer {
                ItemEditViewModel(tripId, itemId, container.tripRepository, container.validateChecklistItem)
            }
        }
    }
}
