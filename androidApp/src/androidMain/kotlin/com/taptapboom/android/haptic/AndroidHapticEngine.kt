package com.taptapboom.android.haptic

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import com.taptapboom.domain.haptic.HapticEngine

/**
 * Android implementation of HapticEngine using the system Vibrator.
 */
class AndroidHapticEngine(context: Context) : HapticEngine {
    
    private val vibrator: Vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
        vibratorManager.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    }

    override fun impact(intensity: Float) {
        if (!vibrator.hasVibrator()) return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Map intensity (0-1) to amplitude (1-255)
            val amplitude = (intensity.coerceIn(0f, 1f) * 255).toInt().coerceAtLeast(1)
            vibrator.vibrate(VibrationEffect.createOneShot(20, amplitude))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(20)
        }
    }

    override fun vibrate(durationMs: Long) {
        if (!vibrator.hasVibrator()) return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(durationMs, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(durationMs)
        }
    }

    override fun release() {
        vibrator.cancel()
    }
}
