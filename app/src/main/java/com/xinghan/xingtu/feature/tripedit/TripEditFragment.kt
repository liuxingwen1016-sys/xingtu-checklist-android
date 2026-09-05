package com.xinghan.xingtu.feature.tripedit

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.NavOptions
import com.google.android.material.snackbar.Snackbar
import com.xinghan.xingtu.R
import com.xinghan.xingtu.core.navigation.appContainer
import com.xinghan.xingtu.core.util.DateFormats
import com.xinghan.xingtu.databinding.FragmentTripEditBinding
import com.xinghan.xingtu.domain.model.TripTemplate
import com.xinghan.xingtu.domain.usecase.DateError
import com.xinghan.xingtu.domain.usecase.FieldError
import com.xinghan.xingtu.feature.tripdetail.TripDetailFragment
import kotlinx.coroutines.launch
import java.time.LocalDate

class TripEditFragment : Fragment() {

    private var _binding: FragmentTripEditBinding? = null
    private val binding get() = _binding!!
    private val tripId get() = arguments?.getString(TripDetailFragment.ARG_TRIP_ID)
    private val viewModel: TripEditViewModel by viewModels {
        TripEditViewModel.provideFactory(appContainer, tripId)
    }
    private var rendering = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, state: Bundle?): View {
        _binding = FragmentTripEditBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.backButton.setOnClickListener { findNavController().navigateUp() }
        binding.titleInput.doAfterTextChanged { if (!rendering) viewModel.updateTitle(it?.toString().orEmpty()) }
        binding.destinationInput.doAfterTextChanged { if (!rendering) viewModel.updateDestination(it?.toString().orEmpty()) }
        binding.noteInput.doAfterTextChanged { if (!rendering) viewModel.updateNote(it?.toString().orEmpty()) }
        binding.startDateButton.setOnClickListener { showDatePicker(viewModel.uiState.value.startDate, viewModel::updateStartDate) }
        binding.endDateButton.setOnClickListener { showDatePicker(viewModel.uiState.value.endDate, viewModel::updateEndDate) }
        binding.themeGroup.setOnCheckedStateChangeListener { _, ids ->
            if (!rendering) viewModel.updateTheme(themeForId(ids.firstOrNull()))
        }
        binding.templateGroup.setOnCheckedChangeListener { _, id ->
            if (!rendering) viewModel.updateTemplate(templateForId(id))
        }
        binding.saveButton.setOnClickListener { viewModel.save() }
        observeViewModel()
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch { viewModel.uiState.collect(::render) }
                launch { viewModel.effects.collect(::handleEffect) }
            }
        }
    }

    private fun render(state: TripEditUiState) {
        rendering = true
        binding.screenTitle.setText(if (state.isEditing) R.string.trip_edit_title_edit else R.string.trip_edit_title_create)
        if (binding.titleInput.text?.toString() != state.title) binding.titleInput.setText(state.title)
        if (binding.destinationInput.text?.toString() != state.destination) binding.destinationInput.setText(state.destination)
        if (binding.noteInput.text?.toString() != state.note) binding.noteInput.setText(state.note)
        binding.titleInputLayout.error = fieldErrorText(state.titleError)
        binding.destinationInputLayout.error = fieldErrorText(state.destinationError)
        binding.startDateButton.text = DateFormats.formatDate(state.startDate)
        binding.endDateButton.text = DateFormats.formatDate(state.endDate)
        binding.dateError.isVisible = state.dateError != DateError.NONE
        binding.templateSection.isVisible = !state.isEditing
        binding.noteSection.isVisible = state.isEditing
        binding.themeGroup.check(idForTheme(state.themeKey))
        binding.templateGroup.check(idForTemplate(state.template))
        binding.loadingIndicator.isVisible = state.loading || state.saving
        binding.saveButton.isEnabled = !state.loading && !state.saving
        binding.saveButton.setText(if (state.isEditing) R.string.button_save_trip else R.string.button_create_trip)
        rendering = false
    }

    private fun fieldErrorText(error: FieldError): String? = when (error) {
        FieldError.NONE -> null
        FieldError.EMPTY -> getString(R.string.error_field_empty)
        FieldError.TOO_LONG -> getString(R.string.error_field_too_long)
    }

    private fun showDatePicker(epochDay: Long, onSelected: (Long) -> Unit) {
        val date = LocalDate.ofEpochDay(epochDay)
        DatePickerDialog(requireContext(), { _, year, month, day ->
            onSelected(LocalDate.of(year, month + 1, day).toEpochDay())
        }, date.year, date.monthValue - 1, date.dayOfMonth).show()
    }

    private fun handleEffect(effect: TripEditEffect) {
        when (effect) {
            is TripEditEffect.ShowMessage -> Snackbar.make(binding.root, effect.message, Snackbar.LENGTH_SHORT).show()
            is TripEditEffect.Saved -> {
                Snackbar.make(binding.root, effect.message, Snackbar.LENGTH_SHORT).show()
                if (tripId == null) {
                    findNavController().navigate(
                        R.id.tripDetailFragment,
                        bundleOf(TripDetailFragment.ARG_TRIP_ID to effect.tripId),
                        NavOptions.Builder()
                            .setPopUpTo(R.id.tripEditFragment, true)
                            .build(),
                    )
                } else {
                    findNavController().navigateUp()
                }
            }
        }
    }

    private fun idForTheme(key: String): Int = when (key) {
        "blue" -> R.id.themeBlue
        "coral" -> R.id.themeCoral
        "teal" -> R.id.themeTeal
        "amber" -> R.id.themeAmber
        "violet" -> R.id.themeViolet
        else -> R.id.themeIndigo
    }

    private fun themeForId(id: Int?): String = when (id) {
        R.id.themeBlue -> "blue"
        R.id.themeCoral -> "coral"
        R.id.themeTeal -> "teal"
        R.id.themeAmber -> "amber"
        R.id.themeViolet -> "violet"
        else -> "indigo"
    }

    private fun idForTemplate(template: TripTemplate): Int = when (template) {
        TripTemplate.BUSINESS -> R.id.templateBusiness
        TripTemplate.WEEKEND -> R.id.templateWeekend
        TripTemplate.CUSTOM -> R.id.templateCustom
    }

    private fun templateForId(id: Int): TripTemplate = when (id) {
        R.id.templateWeekend -> TripTemplate.WEEKEND
        R.id.templateCustom -> TripTemplate.CUSTOM
        else -> TripTemplate.BUSINESS
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
