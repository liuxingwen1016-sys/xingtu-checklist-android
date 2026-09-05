package com.xinghan.xingtu.feature.home

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.graphics.ColorUtils
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.xinghan.xingtu.R
import com.xinghan.xingtu.core.navigation.themeColorRes
import com.xinghan.xingtu.core.util.DateFormats
import com.xinghan.xingtu.databinding.ItemHomeTripCardBinding
import com.xinghan.xingtu.domain.model.TripCard
import com.xinghan.xingtu.domain.model.TripStatus

class HomeTripPagerAdapter(
    private val todayEpochDay: () -> Long,
    private val onCardClick: (String) -> Unit,
) : ListAdapter<TripCard, HomeTripPagerAdapter.TripCardViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TripCardViewHolder {
        val binding = ItemHomeTripCardBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false,
        )
        return TripCardViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TripCardViewHolder, position: Int) {
        holder.bind(getItem(position), position, itemCount)
    }

    inner class TripCardViewHolder(
        private val binding: ItemHomeTripCardBinding,
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(card: TripCard, position: Int, total: Int) = with(binding) {
            val context = root.context
            val accent = ContextCompat.getColor(context, themeColorRes(card.trip.themeKey))
            val gradientEnd = ColorUtils.blendARGB(accent, Color.WHITE, 0.22f)
            cardRoot.background = GradientDrawable(
                GradientDrawable.Orientation.TL_BR,
                intArrayOf(accent, gradientEnd),
            ).apply {
                cornerRadius = 20f * context.resources.displayMetrics.density
            }
            heroTripTitle.text = card.trip.title
            heroTripSubtitle.text = context.getString(
                R.string.home_hero_subtitle,
                card.trip.destination,
                DateFormats.formatDateRange(card.trip.startDate, card.trip.endDate),
            )
            heroProgressRing.setPercent(card.progressPercent)
            heroCompletedText.text = context.getString(
                R.string.home_completed_of,
                card.completedItems,
                card.totalItems,
            )
            heroCountdownChip.text = when (card.status) {
                TripStatus.UPCOMING -> context.getString(
                    R.string.home_countdown_days,
                    DateFormats.countdownDays(card.trip.startDate, todayEpochDay()),
                )
                TripStatus.ONGOING -> context.getString(R.string.home_ongoing_today)
                TripStatus.FINISHED -> context.getString(R.string.status_finished)
            }
            cardRoot.contentDescription = context.getString(
                R.string.home_trip_page_description,
                position + 1,
                total,
                card.trip.title,
            )
            cardRoot.setOnClickListener { onCardClick(card.trip.id) }
        }
    }

    private object DiffCallback : DiffUtil.ItemCallback<TripCard>() {
        override fun areItemsTheSame(oldItem: TripCard, newItem: TripCard): Boolean =
            oldItem.trip.id == newItem.trip.id

        override fun areContentsTheSame(oldItem: TripCard, newItem: TripCard): Boolean =
            oldItem == newItem
    }
}
