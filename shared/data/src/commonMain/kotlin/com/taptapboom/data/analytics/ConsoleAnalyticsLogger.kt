package com.taptapboom.data.analytics

import com.taptapboom.domain.analytics.AnalyticsLogger

/**
 * Basic console implementation of [AnalyticsLogger].
 * In a real app, this would wrap Firebase or Amplitude.
 */
class ConsoleAnalyticsLogger : AnalyticsLogger {
    override fun logAppOpen() {
        println("ANALYTICS: app_open")
    }

    override fun logSoundPlayed(soundId: String) {
        println("ANALYTICS: sound_played [soundId: $soundId]")
    }

    override fun logInteraction(pointersCount: Int) {
        println("ANALYTICS: interaction_tapped [pointers: $pointersCount]")
    }
}
