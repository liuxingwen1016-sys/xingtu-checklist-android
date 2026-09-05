package com.xinghan.xingtu.feature.trips

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
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.xinghan.xingtu.R
import com.xinghan.xingtu.core.navigation.appContainer
import com.xinghan.xingtu.databinding.FragmentTripsBinding
import com.xinghan.xingtu.domain.usecase.TripFilter
import com.xinghan.xingtu.feature.tripdetail.TripDetailFragment
import kotlinx.coroutines.launch

class TripsFragment : Fragment() {

    private var _binding: FragmentTripsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: TripsViewModel by viewModels { TripsViewModel.provideFactory(appContainer) }
    private lateinit var adapter: TripListAdapter
    private val isTwoPane: Boolean
        get() = resources.configuration.smallestScreenWidthDp >= 600

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentTripsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        adapter = TripListAdapter { tripId -> viewModel.onTripClicked(tripId, isTwoPane) }
        binding.tripsList.layoutManager = LinearLayoutManager(requireContext())
        binding.tripsList.adapter = adapter
        binding.searchInput.doAfterTextChanged { viewModel.onQueryChanged(it?.toString().orEmpty()) }
        binding.filterChips.setOnCheckedStateChangeListener { _, checkedIds ->
            val filter = when (checkedIds.firstOrNull()) {
                R.id.chipUpcoming -> TripFilter.UPCOMING
                R.id.chipOngoing -> TripFilter.ONGOING
                R.id.chipFinished -> TripFilter.FINISHED
                else -> TripFilter.ALL
            }
            viewModel.onFilterChanged(filter)
        }
        binding.createTripFab.setOnClickListener { viewModel.onCreateTripClicked() }
        viewModel.onTwoPaneChanged(isTwoPane)
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

    private fun render(state: TripsUiState) {
        if (binding.searchInput.text?.toString() != state.query) {
            binding.searchInput.setText(state.query)
            binding.searchInput.setSelection(state.query.length)
        }
        val chipId = when (state.filter) {
            TripFilter.ALL -> R.id.chipAll
            TripFilter.UPCOMING -> R.id.chipUpcoming
            TripFilter.ONGOING -> R.id.chipOngoing
            TripFilter.FINISHED -> R.id.chipFinished
        }
        if (binding.filterChips.checkedChipId != chipId) binding.filterChips.check(chipId)
        binding.loadingIndicator.isVisible = state.loading
        binding.emptyPanel.isVisible = !state.loading && state.tripCards.isEmpty()
        binding.tripsList.isVisible = !state.loading && state.tripCards.isNotEmpty()
        adapter.submitList(state.tripCards)
        adapter.setSelectedTrip(state.selectedTripId)

        if (isTwoPane) {
            binding.twoPanePlaceholder.isVisible = state.selectedTripId == null
            state.selectedTripId?.let(::showDetailInPane)
        }
    }

    private fun showDetailInPane(tripId: String) {
        val current = childFragmentManager.findFragmentById(R.id.tripDetailContainer)
        if (current is TripDetailFragment && current.tripId == tripId) return
        childFragmentManager.beginTransaction()
            .replace(R.id.tripDetailContainer, TripDetailFragment.newInstance(tripId, embedded = true))
            .commit()
    }

    fun onEmbeddedTripDeleted(tripId: String) {
        viewModel.onDeletedTripRemoved(tripId)
        childFragmentManager.findFragmentById(R.id.tripDetailContainer)?.let {
            childFragmentManager.beginTransaction().remove(it).commit()
        }
    }

    private fun handleEffect(effect: TripsUiEffect) {
        when (effect) {
            is TripsUiEffect.OpenTripDetail -> findNavController().navigate(
                R.id.tripDetailFragment,
                bundleOf(TripDetailFragment.ARG_TRIP_ID to effect.tripId),
            )
            TripsUiEffect.OpenCreateTrip -> findNavController().navigate(R.id.tripEditFragment)
            is TripsUiEffect.ShowMessage -> Snackbar.make(binding.root, effect.resId, Snackbar.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
