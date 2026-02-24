package com.taptapboom.domain.analytics

/**
 * Core interface for tracking application events and interactions.
 */
interface AnalyticsLogger {
    fun logAppOpen()
    fun logSoundPlayed(soundId: String)
    fun logInteraction(pointersCount: Int)
}
