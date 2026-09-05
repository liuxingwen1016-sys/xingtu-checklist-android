package com.xinghan.xingtu.core.navigation

import android.content.Context
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.xinghan.xingtu.AppContainer
import com.xinghan.xingtu.R
import com.xinghan.xingtu.XingtuApplication
import com.xinghan.xingtu.domain.model.ChecklistCategory
import com.xinghan.xingtu.domain.model.TripStatus
import com.xinghan.xingtu.domain.model.TripThemes

/** Access the app-wide manual dependency container from any fragment. */
val Fragment.appContainer: AppContainer
    get() = requireContext().applicationContext.let {
        (it as XingtuApplication).container
    }

val Fragment.hapticClick: Unit
    get() = appContainer.hapticFeedback.confirmation()

@StringRes
fun TripStatus.labelRes(): Int = when (this) {
    TripStatus.UPCOMING -> R.string.status_upcoming
    TripStatus.ONGOING -> R.string.status_ongoing
    TripStatus.FINISHED -> R.string.status_finished
}

@DrawableRes
fun TripStatus.chipBackgroundRes(): Int = when (this) {
    TripStatus.UPCOMING -> R.drawable.bg_status_chip_upcoming
    TripStatus.ONGOING -> R.drawable.bg_status_chip_ongoing
    TripStatus.FINISHED -> R.drawable.bg_status_chip_finished
}

@ColorRes
fun TripStatus.chipTextColorRes(): Int = when (this) {
    TripStatus.UPCOMING -> R.color.statusUpcomingText
    TripStatus.ONGOING -> R.color.statusOngoingText
    TripStatus.FINISHED -> R.color.statusFinishedText
}

@StringRes
fun ChecklistCategory.labelRes(): Int = when (this) {
    ChecklistCategory.DOCUMENT -> R.string.category_document
    ChecklistCategory.DIGITAL -> R.string.category_digital
    ChecklistCategory.CLOTHING -> R.string.category_clothing
    ChecklistCategory.WORK -> R.string.category_work
    ChecklistCategory.OTHER -> R.string.category_other
}

@ColorRes
fun themeColorRes(themeKey: String): Int = when (themeKey) {
    "blue" -> R.color.tripThemeBlue
    "coral" -> R.color.tripThemeCoral
    "teal" -> R.color.tripThemeTeal
    "amber" -> R.color.tripThemeAmber
    "violet" -> R.color.tripThemeViolet
    else -> R.color.tripThemeIndigo
}

@StringRes
fun themeNameRes(themeKey: String): Int = when (themeKey) {
    "blue" -> R.string.theme_name_blue
    "coral" -> R.string.theme_name_coral
    "teal" -> R.string.theme_name_teal
    "amber" -> R.string.theme_name_amber
    "violet" -> R.string.theme_name_violet
    else -> R.string.theme_name_indigo
}

fun Context.color(@ColorRes resId: Int): Int = ContextCompat.getColor(this, resId)

/** All preset theme keys in display order. */
val TRIP_THEME_KEYS: List<String> = TripThemes.ALL
