package com.xinghan.xingtu.feature.itemedit

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.snackbar.Snackbar
import com.xinghan.xingtu.R
import com.xinghan.xingtu.core.navigation.appContainer
import com.xinghan.xingtu.databinding.DialogItemEditBinding
import com.xinghan.xingtu.domain.model.ChecklistCategory
import com.xinghan.xingtu.domain.usecase.FieldError
import com.xinghan.xingtu.feature.tripdetail.TripDetailFragment
import kotlinx.coroutines.launch

class ItemEditDialogFragment : DialogFragment() {
    private var _binding: DialogItemEditBinding? = null
    private val binding get() = _binding!!
    private val tripId get() = requireArguments().getString(TripDetailFragment.ARG_TRIP_ID).orEmpty()
    private val itemId get() = arguments?.getString(TripDetailFragment.ARG_ITEM_ID)
    private val viewModel: ItemEditViewModel by viewModels {
        ItemEditViewModel.provideFactory(appContainer, tripId, itemId)
    }
    private var rendering = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = DialogItemEditBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.92f).toInt(),
            ViewGroup.LayoutParams.WRAP_CONTENT,
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.cancelButton.setOnClickListener { dismiss() }
        binding.titleInput.doAfterTextChanged { if (!rendering) viewModel.updateTitle(it?.toString().orEmpty()) }
        binding.noteInput.doAfterTextChanged { if (!rendering) viewModel.updateNote(it?.toString().orEmpty()) }
        binding.categoryGroup.setOnCheckedStateChangeListener { _, ids ->
            if (!rendering) viewModel.updateCategory(categoryForId(ids.firstOrNull()))
        }
        binding.importantSwitch.setOnCheckedChangeListener { _, checked ->
            if (!rendering) viewModel.updateImportant(checked)
        }
        binding.saveButton.setOnClickListener { viewModel.save() }
        binding.deleteButton.setOnClickListener { confirmDelete() }
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch { viewModel.uiState.collect(::render) }
                launch { viewModel.effects.collect(::handleEffect) }
            }
        }
    }

    private fun render(state: ItemEditUiState) {
        if (_binding == null) return
        rendering = true
        binding.dialogTitle.setText(if (state.isEditing) R.string.item_edit_title_edit else R.string.item_edit_title_create)
        if (binding.titleInput.text?.toString() != state.title) binding.titleInput.setText(state.title)
        if (binding.noteInput.text?.toString() != state.note) binding.noteInput.setText(state.note)
        binding.titleInputLayout.error = errorText(state.titleError)
        binding.noteInputLayout.error = errorText(state.noteError)
        binding.categoryGroup.check(idForCategory(state.category))
        binding.importantSwitch.isChecked = state.important
        binding.deleteButton.isVisible = state.isEditing
        binding.loadingIndicator.isVisible = state.loading || state.saving
        binding.saveButton.isEnabled = !state.loading && !state.saving
        rendering = false
    }

    private fun errorText(error: FieldError): String? = when (error) {
        FieldError.NONE -> null
        FieldError.EMPTY -> getString(R.string.error_field_empty)
        FieldError.TOO_LONG -> getString(R.string.error_field_too_long)
    }

    private fun handleEffect(effect: ItemEditEffect) {
        when (effect) {
            is ItemEditEffect.Done -> {
                Snackbar.make(requireActivity().findViewById(android.R.id.content), effect.message, Snackbar.LENGTH_SHORT).show()
                dismiss()
            }
            is ItemEditEffect.ShowMessage -> Snackbar.make(binding.root, effect.message, Snackbar.LENGTH_SHORT).show()
        }
    }

    private fun confirmDelete() {
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.detail_delete_item_title)
            .setMessage(getString(R.string.detail_delete_item_message, viewModel.uiState.value.title))
            .setNegativeButton(R.string.action_cancel, null)
            .setPositiveButton(R.string.action_delete) { _, _ -> viewModel.delete() }
            .show()
    }

    private fun idForCategory(category: ChecklistCategory): Int = when (category) {
        ChecklistCategory.DOCUMENT -> R.id.categoryDocument
        ChecklistCategory.DIGITAL -> R.id.categoryDigital
        ChecklistCategory.CLOTHING -> R.id.categoryClothing
        ChecklistCategory.WORK -> R.id.categoryWork
        ChecklistCategory.OTHER -> R.id.categoryOther
    }

    private fun categoryForId(id: Int?): ChecklistCategory = when (id) {
        R.id.categoryDocument -> ChecklistCategory.DOCUMENT
        R.id.categoryDigital -> ChecklistCategory.DIGITAL
        R.id.categoryClothing -> ChecklistCategory.CLOTHING
        R.id.categoryWork -> ChecklistCategory.WORK
        else -> ChecklistCategory.OTHER
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
