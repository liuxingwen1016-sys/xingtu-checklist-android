package com.xinghan.xingtu.feature.tripdetail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.xinghan.xingtu.R
import com.xinghan.xingtu.core.navigation.appContainer
import com.xinghan.xingtu.core.navigation.hapticClick
import com.xinghan.xingtu.core.navigation.labelRes
import com.xinghan.xingtu.core.util.DateFormats
import com.xinghan.xingtu.databinding.FragmentTripDetailBinding
import com.xinghan.xingtu.databinding.ItemChecklistCompactBinding
import com.xinghan.xingtu.databinding.ItemChecklistGroupBinding
import com.xinghan.xingtu.domain.model.ChecklistCategory
import com.xinghan.xingtu.domain.model.ChecklistItem
import com.xinghan.xingtu.domain.model.TripDetail
import com.xinghan.xingtu.feature.trips.TripsFragment
import com.xinghan.xingtu.core.navigation.themeColorRes
import androidx.core.content.ContextCompat
import kotlinx.coroutines.launch

class TripDetailFragment : Fragment() {

    private var _binding: FragmentTripDetailBinding? = null
    private val binding get() = _binding!!
    val tripId: String get() = requireArguments().getString(ARG_TRIP_ID).orEmpty()
    private val embedded: Boolean get() = requireArguments().getBoolean(ARG_EMBEDDED, false)
    private val viewModel: TripDetailViewModel by viewModels {
        TripDetailViewModel.provideFactory(appContainer, tripId)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, state: Bundle?): View {
        _binding = FragmentTripDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.backButton.isVisible = !embedded
        binding.backButton.setOnClickListener { findNavController().navigateUp() }
        binding.editButton.setOnClickListener { openTripEditor() }
        binding.shareButton.setOnClickListener { shareTrip() }
        binding.deleteTripButton.setOnClickListener { confirmDeleteTrip() }
        binding.addItemButton.setOnClickListener { openItemEditor(null) }
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

    private fun render(state: TripDetailUiState) {
        binding.loadingIndicator.isVisible = state.loading
        binding.notFoundPanel.isVisible = !state.loading && state.detail == null
        binding.contentPanel.isVisible = !state.loading && state.detail != null
        val detail = state.detail ?: return
        val card = state.card ?: return
        binding.tripTitle.text = detail.trip.title
        binding.tripSubtitle.text = getString(
            R.string.home_hero_subtitle,
            detail.trip.destination,
            DateFormats.formatDateRange(detail.trip.startDate, detail.trip.endDate),
        )
        binding.statusText.text = getString(card.status.labelRes())
        binding.progressRing.setPercent(card.progressPercent)
        binding.heroCard.setCardBackgroundColor(
            ContextCompat.getColor(requireContext(), themeColorRes(detail.trip.themeKey))
        )
        binding.completedText.text = getString(
            R.string.home_completed_of, card.completedItems, card.totalItems,
        )
        binding.noteCard.isVisible = !detail.trip.note.isNullOrBlank()
        binding.noteText.text = detail.trip.note.orEmpty()
        binding.emptyItemsText.isVisible = detail.items.isEmpty()
        renderItems(detail)
    }

    private fun renderItems(detail: TripDetail) {
        binding.itemsContainer.removeAllViews()
        val grouped = detail.items.groupBy { it.category }
        ChecklistCategory.DISPLAY_ORDER.forEach { category ->
            val categoryItems = grouped[category].orEmpty().sortedBy { it.sortOrder }
            if (categoryItems.isEmpty()) return@forEach
            val group = ItemChecklistGroupBinding.inflate(layoutInflater, binding.itemsContainer, false)
            group.categoryTitle.text = getString(category.labelRes())
            group.categoryCount.text = "${categoryItems.count { it.isCompleted }}/${categoryItems.size}"
            group.categoryIcon.text = getString(category.labelRes()).take(1)
            categoryItems.forEach { item ->
                val row = ItemChecklistCompactBinding.inflate(layoutInflater, group.categoryItems, false)
                row.itemTitle.text = item.title
                row.importantTag.isVisible = item.priority.isImportant
                row.itemNote.isVisible = !item.note.isNullOrBlank()
                row.itemNote.text = item.note.orEmpty()
                row.itemCheck.isChecked = item.isCompleted
                row.itemTitle.alpha = if (item.isCompleted) 0.52f else 1f
                row.itemCheck.setOnClickListener {
                    hapticClick
                    viewModel.toggleItem(item.id, !item.isCompleted)
                }
                row.root.setOnClickListener { openItemEditor(item.id) }
                row.root.setOnLongClickListener {
                    confirmDeleteItem(item)
                    true
                }
                group.categoryItems.addView(row.root)
            }
            binding.itemsContainer.addView(group.root)
        }
    }

    private fun openTripEditor() {
        findNavController().navigate(R.id.tripEditFragment, bundleOf(ARG_TRIP_ID to tripId))
    }

    private fun openItemEditor(itemId: String?) {
        findNavController().navigate(
            R.id.itemEditDialog,
            bundleOf(ARG_TRIP_ID to tripId, ARG_ITEM_ID to itemId),
        )
    }

    private fun shareTrip() {
        viewModel.uiState.value.card?.let {
            appContainer.shareLauncher.shareText(appContainer.buildShareSummary(it))
        }
    }

    private fun confirmDeleteTrip() {
        val title = viewModel.uiState.value.detail?.trip?.title ?: return
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.detail_delete_trip_title)
            .setMessage(getString(R.string.detail_delete_trip_message, title))
            .setNegativeButton(R.string.action_cancel, null)
            .setPositiveButton(R.string.action_delete) { _, _ -> viewModel.deleteTrip() }
            .show()
    }

    private fun confirmDeleteItem(item: ChecklistItem) {
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.detail_delete_item_title)
            .setMessage(getString(R.string.detail_delete_item_message, item.title))
            .setNegativeButton(R.string.action_cancel, null)
            .setPositiveButton(R.string.action_delete) { _, _ -> viewModel.deleteItem(item.id) }
            .show()
    }

    private fun handleEffect(effect: TripDetailEffect) {
        when (effect) {
            is TripDetailEffect.ShowMessage -> Snackbar.make(binding.root, effect.resId, Snackbar.LENGTH_SHORT).show()
            TripDetailEffect.TripDeleted -> {
                if (embedded) {
                    (parentFragment as? TripsFragment)?.onEmbeddedTripDeleted(tripId)
                } else {
                    findNavController().navigateUp()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val ARG_TRIP_ID = "tripId"
        const val ARG_ITEM_ID = "itemId"
        private const val ARG_EMBEDDED = "embedded"

        fun newInstance(tripId: String, embedded: Boolean = false) = TripDetailFragment().apply {
            arguments = bundleOf(ARG_TRIP_ID to tripId, ARG_EMBEDDED to embedded)
        }
    }
}
