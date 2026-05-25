package com.maku.eyeonsite.di

import com.maku.eyeonsite.analytics.AnalyticsService
import com.maku.eyeonsite.analytics.FirebaseAnalyticsService
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.dsl.module

actual val analyticsModule: Module =
    module {
        single<AnalyticsService> { FirebaseAnalyticsService(androidContext()) }
    }
