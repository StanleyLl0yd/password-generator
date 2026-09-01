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

    private fun performVibration(context: Context, duration: Long) {
        try {
            val vibrator = getVibrator(context) ?: return
            val effect = VibrationEffect.createOneShot(
                duration,
                VibrationEffect.DEFAULT_AMPLITUDE
            )
            vibrator.vibrate(effect)
        } catch (_: Exception) {
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
