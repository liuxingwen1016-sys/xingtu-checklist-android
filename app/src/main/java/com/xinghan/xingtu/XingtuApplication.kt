package com.xinghan.xingtu

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import com.xinghan.xingtu.domain.model.ThemeMode
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

class XingtuApplication : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
        val themeMode = runBlocking { container.settingsRepository.observeThemeMode().first() }
        AppCompatDelegate.setDefaultNightMode(
            when (themeMode) {
                ThemeMode.SYSTEM -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
                ThemeMode.LIGHT -> AppCompatDelegate.MODE_NIGHT_NO
                ThemeMode.DARK -> AppCompatDelegate.MODE_NIGHT_YES
            }
        )
        container.notificationService.createChannel()
        container.seedDemoDataOnFirstLaunch()
        container.startWidgetUpdates()
    }
}
