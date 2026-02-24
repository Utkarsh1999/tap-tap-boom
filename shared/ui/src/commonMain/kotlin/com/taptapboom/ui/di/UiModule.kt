package com.taptapboom.ui.di

import com.taptapboom.ui.viewmodel.CanvasViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

/**
 * Koin DI module for the shared UI layer.
 */
val uiModule = module {
    viewModel { CanvasViewModel(get(), get(), get(), get()) }
}
