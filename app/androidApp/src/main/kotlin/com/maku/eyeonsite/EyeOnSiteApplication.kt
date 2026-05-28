package com.maku.eyeonsite

import android.app.Application
import com.google.firebase.FirebaseApp
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory
import com.maku.eyeonsite.di.analyticsModule
import com.maku.eyeonsite.di.authModule
import com.maku.eyeonsite.sync.UploadSyncScheduler
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class EyeOnSiteApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        installAppCheck()
        startKoin {
            androidContext(this@EyeOnSiteApplication)
            modules(authModule, analyticsModule)
        }
        UploadSyncScheduler.schedulePeriodicSync(this)
    }

    private fun installAppCheck() {
        FirebaseApp.initializeApp(this)
        val providerFactory =
            if (BuildConfig.DEBUG) {
                DebugAppCheckProviderFactory.getInstance()
            } else {
                PlayIntegrityAppCheckProviderFactory.getInstance()
            }
        FirebaseAppCheck.getInstance().installAppCheckProviderFactory(providerFactory)
    }
}
