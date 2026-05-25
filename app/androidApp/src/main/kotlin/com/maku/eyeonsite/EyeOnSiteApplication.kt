package com.maku.eyeonsite

import android.app.Application
import com.maku.eyeonsite.di.analyticsModule
import com.maku.eyeonsite.di.authModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class EyeOnSiteApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@EyeOnSiteApplication)
            modules(authModule, analyticsModule)
        }
    }
}
