package com.xinghan.xingtu.feature.stats

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.xinghan.xingtu.R
import com.xinghan.xingtu.core.navigation.appContainer
import com.xinghan.xingtu.core.navigation.labelRes
import com.xinghan.xingtu.databinding.FragmentStatsBinding
import com.xinghan.xingtu.databinding.ItemCategoryStatBinding
import com.xinghan.xingtu.domain.model.DayCompletion
import com.xinghan.xingtu.domain.model.TripStatistics
import kotlinx.coroutines.launch
import java.time.LocalDate

class StatsFragment : Fragment() {

    private var _binding: FragmentStatsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: StatsViewModel by viewModels { StatsViewModel.provideFactory(appContainer) }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentStatsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect(::render)
            }
        }
    }

    private fun render(state: StatsUiState) {
        binding.loadingIndicator.isVisible = state.loading
        binding.contentPanel.isVisible = !state.loading && (state.statistics?.tripCount ?: 0) > 0
        binding.emptyPanel.isVisible = !state.loading && (state.statistics?.tripCount ?: 0) == 0
        state.statistics?.takeIf { it.tripCount > 0 }?.let(::renderStatistics)
    }

    private fun renderStatistics(statistics: TripStatistics) {
        binding.tripCount.text = statistics.tripCount.toString()
        binding.finishedCount.text = statistics.completedItemCount.toString()
        binding.completionPercent.text = "${statistics.completionPercent}%"
        binding.completionRing.setData(statistics.categoryStats, statistics.completionPercent)
        renderTrend(statistics.sevenDaySeries)
        binding.categoryContainer.removeAllViews()
        statistics.categoryStats.forEach { stat ->
            val row = ItemCategoryStatBinding.inflate(layoutInflater, binding.categoryContainer, false)
            row.categoryName.text = getString(stat.category.labelRes())
            row.categoryCount.text = "${stat.completed}/${stat.total}"
            row.categoryPercent.text = "${stat.percent}%"
            row.categoryProgress.progress = stat.percent
            binding.categoryContainer.addView(row.root)
        }
    }

    private fun renderTrend(series: List<DayCompletion>) {
        binding.trendContainer.removeAllViews()
        val max = series.maxOfOrNull { it.count }?.coerceAtLeast(1) ?: 1
        series.forEach { day ->
            val column = LinearLayout(requireContext()).apply {
                orientation = LinearLayout.VERTICAL
                gravity = android.view.Gravity.BOTTOM or android.view.Gravity.CENTER_HORIZONTAL
                layoutParams = LinearLayout.LayoutParams(0, dp(100), 1f)
            }
            column.addView(TextView(requireContext()).apply {
                text = day.count.toString()
                textSize = 11f
                setTextColor(ContextCompat.getColor(requireContext(), R.color.colorTextSecondary))
                gravity = android.view.Gravity.CENTER
            }, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(24)))
            column.addView(View(requireContext()).apply {
                setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.chartBarPrimary))
            }, LinearLayout.LayoutParams(dp(18), dp(48) * day.count / max + dp(4)))
            val date = LocalDate.ofEpochDay(day.epochDay)
            column.addView(TextView(requireContext()).apply {
                text = "${date.monthValue}/${date.dayOfMonth}"
                textSize = 10f
                setTextColor(ContextCompat.getColor(requireContext(), R.color.colorTextSecondary))
                gravity = android.view.Gravity.CENTER
            }, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(30)))
            binding.trendContainer.addView(column)
        }
    }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
