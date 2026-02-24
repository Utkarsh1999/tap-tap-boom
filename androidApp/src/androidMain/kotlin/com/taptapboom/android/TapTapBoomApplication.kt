package com.taptapboom.android

import android.app.Application
import com.taptapboom.android.di.androidModule
import com.taptapboom.data.di.dataModule
import com.taptapboom.domain.di.domainModule
import com.taptapboom.ui.di.uiModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

/**
 * Application class that initializes Koin DI with all modules.
 */
class TapTapBoomApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@TapTapBoomApplication)
            modules(
                domainModule,
                dataModule,
                uiModule,
                androidModule
            )
        }
    }
}
