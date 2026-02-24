package com.taptapboom.domain.di

import com.taptapboom.domain.usecase.PreloadSoundsUseCase
import com.taptapboom.domain.usecase.TriggerInteractionUseCase
import org.koin.dsl.module

/**
 * Koin DI module for the domain layer.
 */
val domainModule = module {
    factory { TriggerInteractionUseCase(get()) }
    factory { PreloadSoundsUseCase(get(), get()) }
}
