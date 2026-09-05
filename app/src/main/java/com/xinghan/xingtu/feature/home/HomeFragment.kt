package com.xinghan.xingtu.feature.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.xinghan.xingtu.R
import com.xinghan.xingtu.core.navigation.appContainer
import com.xinghan.xingtu.core.navigation.hapticClick
import com.xinghan.xingtu.databinding.FragmentHomeBinding
import com.xinghan.xingtu.databinding.ItemHomeTodoBinding
import com.xinghan.xingtu.domain.model.PendingItem
import kotlinx.coroutines.launch
import java.time.LocalTime

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val viewModel: HomeViewModel by viewModels { HomeViewModel.provideFactory(appContainer) }
    private lateinit var heroAdapter: HomeTripPagerAdapter
    private lateinit var heroLayoutManager: LinearLayoutManager
    private val heroSnapHelper = PagerSnapHelper()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupHeroPager()
        setupActions()
        observeViewModel()
    }

    private fun setupActions() {
        binding.notificationButton.setOnClickListener { findNavController().navigate(R.id.nativeLabFragment) }
        binding.createTripButton.setOnClickListener { viewModel.onCreateTripClicked() }
        binding.emptyCreateButton.setOnClickListener { viewModel.onCreateTripClicked() }
        binding.retryButton.setOnClickListener { viewModel.retry() }
    }

    private fun setupHeroPager() {
        heroAdapter = HomeTripPagerAdapter(
            todayEpochDay = { appContainer.dateProvider.todayEpochDay() },
            onCardClick = { tripId ->
                hapticClick
                viewModel.onHeroCardClicked(tripId)
            },
        )
        heroLayoutManager = LinearLayoutManager(
            requireContext(),
            LinearLayoutManager.HORIZONTAL,
            false,
        )
        binding.heroPager.layoutManager = heroLayoutManager
        binding.heroPager.adapter = heroAdapter
        heroSnapHelper.attachToRecyclerView(binding.heroPager)
        binding.heroPager.addOnScrollListener(
            object : RecyclerView.OnScrollListener() {
                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                    if (newState != RecyclerView.SCROLL_STATE_IDLE) return
                    val snapped = heroSnapHelper.findSnapView(heroLayoutManager) ?: return
                    val position = heroLayoutManager.getPosition(snapped)
                    val tripId = heroAdapter.currentList.getOrNull(position)?.trip?.id ?: return
                    viewModel.onHeroPageSelected(tripId)
                }
            }
        )
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch { viewModel.uiState.collect { render(it) } }
                launch { viewModel.effects.collect { handleEffect(it) } }
            }
        }
    }

    private fun render(state: HomeUiState) {
        binding.loadingIndicator.isVisible = state.loading
        binding.errorPanel.isVisible = state.failed
        binding.greetingText.text = greetingText()
        val hasTrip = state.tripCards.isNotEmpty()
        binding.contentPanel.isVisible = !state.loading && !state.failed && hasTrip
        binding.emptyPanel.isVisible = !state.loading && !state.failed && !hasTrip

        if (!hasTrip) return
        val selectedIndex = state.tripCards
            .indexOfFirst { it.trip.id == state.selectedTripId }
            .coerceAtLeast(0)
        heroAdapter.submitList(state.tripCards) {
            if (heroLayoutManager.findFirstVisibleItemPosition() != selectedIndex) {
                binding.heroPager.scrollToPosition(selectedIndex)
            }
            renderHeroIndicator(state.tripCards.size, selectedIndex)
        }
        renderPendingItems(state.pendingItems)
    }

    private fun renderHeroIndicator(pageCount: Int, selectedIndex: Int) {
        binding.heroIndicator.isVisible = pageCount > 1
        binding.heroIndicator.removeAllViews()
        repeat(pageCount) { index ->
            binding.heroIndicator.addView(
                View(requireContext()).apply {
                    setBackgroundResource(
                        if (index == selectedIndex) {
                            R.drawable.bg_pager_dot_active
                        } else {
                            R.drawable.bg_pager_dot_inactive
                        }
                    )
                },
                LinearLayout.LayoutParams(dp(7), dp(7)).apply {
                    marginStart = dp(3)
                    marginEnd = dp(3)
                },
            )
        }
    }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()

    private fun renderPendingItems(items: List<PendingItem>) {
        val list = binding.pendingList
        list.removeAllViews()
        items.forEach { pending ->
            val itemBinding = ItemHomeTodoBinding.inflate(layoutInflater, list, false)
            itemBinding.itemTitle.text = pending.item.title
            itemBinding.importantTag.isVisible = pending.item.priority.isImportant
            itemBinding.itemCheck.isChecked = pending.item.isCompleted
            itemBinding.itemCheck.setOnClickListener {
                hapticClick
                viewModel.onToggleItemCompleted(pending.item.id, !pending.item.isCompleted)
            }
            itemBinding.root.setOnClickListener { viewModel.onPendingItemClicked(pending.trip.id) }
            list.addView(itemBinding.root)
        }
    }

    private fun handleEffect(effect: HomeUiEffect) {
        when (effect) {
            is HomeUiEffect.OpenTripDetail -> findNavController().navigate(
                R.id.tripDetailFragment,
                bundleOf(ARG_TRIP_ID to effect.tripId),
            )
            is HomeUiEffect.OpenCreateTrip -> findNavController().navigate(R.id.tripEditFragment)
            is HomeUiEffect.ShowMessage -> Snackbar.make(binding.root, effect.resId, Snackbar.LENGTH_SHORT).show()
        }
    }

    private fun greetingText(): String {
        val hour = LocalTime.now().hour
        val resId = when {
            hour < 12 -> R.string.home_greeting_morning
            hour < 18 -> R.string.home_greeting_afternoon
            else -> R.string.home_greeting_evening
        }
        return getString(resId)
    }

    override fun onDestroyView() {
        heroSnapHelper.attachToRecyclerView(null)
        binding.heroPager.adapter = null
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val ARG_TRIP_ID = "tripId"
    }
}
