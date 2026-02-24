package com.taptapboom.domain.haptic

/**
 * Interface for physical/haptic feedback.
 * Decoupled from platform-specific APIs (Vibrator on Android, Taptic on iOS).
 */
interface HapticEngine {
    /**
     * Trigger a sharp, transient impact (common for taps/collisions).
     * @param intensity 0.0 to 1.0 (some platforms may ignore this)
     */
    fun impact(intensity: Float = 1.0f)

    /**
     * Continuous vibration for a specific duration.
     */
    fun vibrate(durationMs: Long)

    /**
     * Release system resources.
     */
    fun release()
}
