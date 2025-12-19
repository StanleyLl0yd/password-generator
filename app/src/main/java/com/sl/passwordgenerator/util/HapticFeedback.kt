package com.sl.passwordgenerator.util

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

object HapticFeedback {

    fun performLight(context: Context) {
        performVibration(context, duration = 20)
    }

    fun performMedium(context: Context) {
        performVibration(context, duration = 40)
    }

    @Suppress("DEPRECATION")
    private fun performVibration(context: Context, duration: Long) {
        try {
            val vibrator = getVibrator(context) ?: return

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val effect = VibrationEffect.createOneShot(
                    duration,
                    VibrationEffect.DEFAULT_AMPLITUDE
                )
                vibrator.vibrate(effect)
            } else {
                vibrator.vibrate(duration)
            }
        } catch (_: Exception) {  // ← ИСПРАВЛЕНО: e → _
            // Ignore vibration errors
        }
    }

    @Suppress("DEPRECATION")
    private fun getVibrator(context: Context): Vibrator? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vibratorManager?.defaultVibrator
        } else {
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    }
}