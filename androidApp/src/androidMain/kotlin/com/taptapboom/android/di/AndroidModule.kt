package com.taptapboom.android.di

import com.taptapboom.android.audio.AndroidAudioEngine
import com.taptapboom.android.haptic.AndroidHapticEngine
import com.taptapboom.data.loader.SoundPackLoader
import com.taptapboom.domain.audio.AudioEngine
import com.taptapboom.domain.haptic.HapticEngine
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

/**
 * Android-specific Koin DI module.
 * Provides platform bindings for AudioEngine and SoundPackLoader.
 */
val androidModule = module {
    // Bind AudioEngine to Android SoundPool implementation
    single<AudioEngine> { AndroidAudioEngine(androidContext()) }

    // Bind HapticEngine to Android implementation
    single<HapticEngine> { AndroidHapticEngine(androidContext()) }

    // Provide platform-specific asset reader for SoundPackLoader
    single {
        SoundPackLoader { assetPath ->
            androidContext().assets.open(assetPath).bufferedReader().readText()
        }
    }
}
