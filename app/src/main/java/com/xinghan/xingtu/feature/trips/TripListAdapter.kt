package com.xinghan.xingtu.feature.trips

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.xinghan.xingtu.core.navigation.chipBackgroundRes
import com.xinghan.xingtu.core.navigation.chipTextColorRes
import com.xinghan.xingtu.core.navigation.labelRes
import com.xinghan.xingtu.core.util.DateFormats
import com.xinghan.xingtu.databinding.ItemTripCardBinding
import com.xinghan.xingtu.domain.model.TripCard

/**
 * Adapter for trip cards in the trips list. Selection highlight is used by
 * the sw600dp two-pane layout.
 */
class TripListAdapter(
    private val onTripClicked: (String) -> Unit,
) : ListAdapter<TripCard, TripListAdapter.TripViewHolder>(TripDiff) {

    private var selectedTripId: String? = null

    fun setSelectedTrip(tripId: String?) {
        if (selectedTripId == tripId) return
        val previous = selectedTripId
        selectedTripId = tripId
        currentList.forEachIndexed { index, card ->
            if (card.trip.id == tripId || card.trip.id == previous) {
                notifyItemChanged(index)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TripViewHolder {
        val binding = ItemTripCardBinding.inflate(
            LayoutInflater.from(parent.context), parent, false,
        )
        return TripViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TripViewHolder, position: Int) {
        holder.bind(getItem(position), selectedTripId)
    }

    inner class TripViewHolder(
        private val binding: ItemTripCardBinding,
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(card: TripCard, selectedId: String?) {
            val context = binding.root.context
            binding.tripTitle.text = card.trip.title
            binding.subtitleText.text = context.getString(
                com.xinghan.xingtu.R.string.home_hero_subtitle,
                card.trip.destination,
                DateFormats.formatDateRange(card.trip.startDate, card.trip.endDate),
            )
            binding.statusChip.text = context.getString(card.status.labelRes())
            binding.statusChip.setBackgroundResource(card.status.chipBackgroundRes())
            binding.statusChip.setTextColor(ContextCompat.getColor(context, card.status.chipTextColorRes()))
            binding.completedText.text = context.getString(
                com.xinghan.xingtu.R.string.home_completed_of,
                card.completedItems,
                card.totalItems,
            )
            binding.tripCover.setImageResource(
                if (card.trip.destination.contains("杭州")) {
                    com.xinghan.xingtu.R.drawable.bg_cover_hangzhou
                } else {
                    com.xinghan.xingtu.R.drawable.bg_cover_shenzhen
                }
            )
            binding.progressRing.setPercent(card.progressPercent)
            binding.root.strokeColor = ContextCompat.getColor(
                context,
                if (card.trip.id == selectedId) {
                    com.xinghan.xingtu.R.color.colorPrimary
                } else {
                    com.xinghan.xingtu.R.color.colorDivider
                },
            )
            binding.root.strokeWidth = if (card.trip.id == selectedId) 2 else 1
            binding.root.setOnClickListener { onTripClicked(card.trip.id) }
        }
    }

    private object TripDiff : DiffUtil.ItemCallback<TripCard>() {
        override fun areItemsTheSame(oldItem: TripCard, newItem: TripCard): Boolean =
            oldItem.trip.id == newItem.trip.id

        override fun areContentsTheSame(oldItem: TripCard, newItem: TripCard): Boolean =
            oldItem == newItem
    }
}
