package com.maku.eyeonsite

import android.app.Application
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
        // Install App Check before any Firebase service is accessed.
        // Firebase auto-initialises via FirebaseInitProvider (before onCreate),
        // so initializeApp() must NOT be called here — it would race with
        // the auto-refresh and trigger Play Integrity before our provider is set.
        val providerFactory =
            if (BuildConfig.DEBUG) {
                DebugAppCheckProviderFactory.getInstance()
            } else {
                PlayIntegrityAppCheckProviderFactory.getInstance()
            }
        FirebaseAppCheck.getInstance().installAppCheckProviderFactory(providerFactory)
        startKoin {
            androidContext(this@EyeOnSiteApplication)
            modules(authModule, analyticsModule)
        }
        UploadSyncScheduler.schedulePeriodicSync(this)
    }
}
