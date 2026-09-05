package com.xinghan.xingtu

import android.content.Context
import androidx.room.Room
import com.xinghan.xingtu.core.util.DateFormats
import com.xinghan.xingtu.data.SystemDateProvider
import com.xinghan.xingtu.data.local.db.XingtuDatabase
import com.xinghan.xingtu.data.repository.SettingsRepositoryImpl
import com.xinghan.xingtu.data.repository.TripRepositoryImpl
import com.xinghan.xingtu.domain.model.DateProvider
import com.xinghan.xingtu.domain.repository.SettingsRepository
import com.xinghan.xingtu.domain.repository.TripRepository
import com.xinghan.xingtu.domain.usecase.BuildShareSummaryUseCase
import com.xinghan.xingtu.domain.usecase.BuildStatisticsUseCase
import com.xinghan.xingtu.domain.usecase.CalculateTripProgressUseCase
import com.xinghan.xingtu.domain.usecase.CalculateTripStatusUseCase
import com.xinghan.xingtu.domain.usecase.FilterAndSortTripsUseCase
import com.xinghan.xingtu.domain.usecase.SeedDemoDataUseCase
import com.xinghan.xingtu.domain.usecase.ValidateChecklistItemUseCase
import com.xinghan.xingtu.domain.usecase.ValidateTripInputUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import com.xinghan.xingtu.platform.widget.XingtuWidgetProvider

/**
 * Manual dependency container. Created once in [XingtuApplication];
 * nothing here touches the Android UI layer.
 */
class AppContainer(context: Context) {

    private val appContext = context.applicationContext

    val dateProvider: DateProvider = SystemDateProvider()

    // Domain rules
    val calculateTripStatus = CalculateTripStatusUseCase()
    val calculateTripProgress = CalculateTripProgressUseCase()
    val buildStatistics = BuildStatisticsUseCase(calculateTripStatus, calculateTripProgress)
    val validateTripInput = ValidateTripInputUseCase()
    val validateChecklistItem = ValidateChecklistItemUseCase()
    val filterAndSortTrips = FilterAndSortTripsUseCase()
    val buildShareSummary = BuildShareSummaryUseCase(DateFormats::formatDateRange)

    // Data layer
    private val database: XingtuDatabase =
        Room.databaseBuilder(appContext, XingtuDatabase::class.java, DB_NAME)
            .build()

    val tripRepository: TripRepository = TripRepositoryImpl(
        db = database,
        tripDao = database.tripDao(),
        itemDao = database.checklistItemDao(),
        dateProvider = dateProvider,
        calculateTripStatus = calculateTripStatus,
        calculateTripProgress = calculateTripProgress,
        buildStatistics = buildStatistics,
    )

    val settingsRepository: SettingsRepository = SettingsRepositoryImpl(appContext)

    val seedDemoData = SeedDemoDataUseCase(tripRepository)

    // Platform services
    val hapticFeedback = com.xinghan.xingtu.platform.haptics.HapticFeedback(appContext, settingsRepository)
    val shareLauncher = com.xinghan.xingtu.platform.share.ShareLauncher(appContext)
    val notificationService = com.xinghan.xingtu.platform.notification.NotificationService(appContext)

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    /**
     * Seeds the fixed demo dataset on first launch (preference-gated).
     * Runs in the background so the first frame is never blocked.
     */
    fun seedDemoDataOnFirstLaunch() {
        applicationScope.launch {
            if (!settingsRepository.getHasSeededDemoData()) {
                runCatching { seedDemoData() }
                    .onSuccess { settingsRepository.setHasSeededDemoData(true) }
            }
        }
    }

    /** Keeps every placed home-screen widget in sync with Room changes. */
    fun startWidgetUpdates() {
        applicationScope.launch {
            tripRepository.observeDashboard().collect { dashboard ->
                XingtuWidgetProvider.updateAll(appContext, dashboard)
            }
        }
    }

    companion object {
        const val DB_NAME = "xingtu.db"
    }
}
