package com.xinghan.xingtu.platform.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.RemoteViews
import com.xinghan.xingtu.MainActivity
import com.xinghan.xingtu.R
import com.xinghan.xingtu.XingtuApplication
import com.xinghan.xingtu.core.util.DateFormats
import com.xinghan.xingtu.domain.model.DashboardData
import com.xinghan.xingtu.domain.model.TripStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class XingtuWidgetProvider : AppWidgetProvider() {
    override fun onUpdate(context: Context, manager: AppWidgetManager, ids: IntArray) {
        val pendingResult = goAsync()
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            runCatching {
                val container = (context.applicationContext as XingtuApplication).container
                val dashboard = container.tripRepository.observeDashboard().first()
                ids.forEach { manager.updateAppWidget(it, buildViews(context, dashboard)) }
            }
            pendingResult.finish()
        }
    }

    companion object {
        fun updateAll(context: Context, dashboard: DashboardData) {
            val manager = AppWidgetManager.getInstance(context)
            val component = ComponentName(context, XingtuWidgetProvider::class.java)
            manager.getAppWidgetIds(component).forEach {
                manager.updateAppWidget(it, buildViews(context, dashboard))
            }
        }

        private fun buildViews(context: Context, dashboard: DashboardData): RemoteViews {
            val views = RemoteViews(context.packageName, R.layout.widget_next_trip)
            val card = dashboard.nextTrip
            if (card == null) {
                views.setTextViewText(R.id.widgetTripTitle, context.getString(R.string.widget_no_trip_title))
                views.setTextViewText(R.id.widgetTripSubtitle, context.getString(R.string.widget_no_trip_subtitle))
                views.setTextViewText(R.id.widgetCountdown, context.getString(R.string.home_create_trip))
                views.setTextViewText(R.id.widgetProgressText, "0%")
                views.setProgressBar(R.id.widgetProgress, 100, 0, false)
            } else {
                views.setTextViewText(R.id.widgetTripTitle, card.trip.title)
                views.setTextViewText(
                    R.id.widgetTripSubtitle,
                    "${card.trip.destination} · ${DateFormats.formatDateRange(card.trip.startDate, card.trip.endDate)}",
                )
                val countdown = when (card.status) {
                    TripStatus.UPCOMING -> context.getString(
                        R.string.widget_countdown_days,
                        DateFormats.countdownDays(card.trip.startDate, java.time.LocalDate.now().toEpochDay()),
                    )
                    TripStatus.ONGOING -> context.getString(R.string.widget_ongoing_today)
                    TripStatus.FINISHED -> context.getString(R.string.widget_trip_finished)
                }
                views.setTextViewText(R.id.widgetCountdown, countdown)
                views.setTextViewText(R.id.widgetProgressText, "${card.progressPercent}%")
                views.setProgressBar(R.id.widgetProgress, 100, card.progressPercent, false)
            }
            val intent = if (card == null) {
                Intent(context, MainActivity::class.java)
            } else {
                Intent(Intent.ACTION_VIEW, Uri.parse("xingtu://trip/${card.trip.id}"), context, MainActivity::class.java)
            }
            val pendingIntent = PendingIntent.getActivity(
                context,
                2001,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )
            views.setOnClickPendingIntent(R.id.widgetRoot, pendingIntent)
            return views
        }
    }
}
