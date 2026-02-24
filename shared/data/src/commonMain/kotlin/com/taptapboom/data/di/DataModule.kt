package com.taptapboom.data.di

import com.taptapboom.data.loader.AudioBufferCache
import com.taptapboom.data.loader.SoundPackLoader
import com.taptapboom.data.repository.SoundRepositoryImpl
import com.taptapboom.domain.repository.SoundRepository
import com.taptapboom.data.analytics.ConsoleAnalyticsLogger
import com.taptapboom.domain.analytics.AnalyticsLogger
import org.koin.dsl.module

/**
 * Koin DI module for the data layer.
 * Note: SoundPackLoader requires a platform-specific assetReader
 * which is provided by the platform module.
 */
val dataModule = module {
    single<SoundRepository> { SoundRepositoryImpl(get()) }
    single { AudioBufferCache() }
    single<AnalyticsLogger> { ConsoleAnalyticsLogger() }
    // SoundPackLoader is provided by platform modules since assetReader is platform-specific
}
