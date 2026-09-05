package com.xinghan.xingtu.platform.haptics

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import com.xinghan.xingtu.domain.repository.SettingsRepository

/**
 * Small haptic helper for key actions. Honors the user setting:
 * when haptics are disabled no system vibration API is called.
 */
class HapticFeedback(
    private val context: Context,
    private val settings: SettingsRepository,
) {

    /** Short confirmation pulse for checkbox toggles and saves. */
    fun confirmation() {
        if (!settings.isHapticEnabled()) return
        vibrate(CONFIRMATION_MS)
    }

    private fun vibrate(millis: Long) {
        val vibrator: Vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val manager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            manager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
        if (!vibrator.hasVibrator()) return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(
                VibrationEffect.createOneShot(millis, VibrationEffect.DEFAULT_AMPLITUDE)
            )
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(millis)
        }
    }

    companion object {
        private const val CONFIRMATION_MS = 30L
    }
}
