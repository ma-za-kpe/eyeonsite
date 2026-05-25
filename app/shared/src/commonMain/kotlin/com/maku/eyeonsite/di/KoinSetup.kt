package com.maku.eyeonsite.di

import org.koin.core.context.startKoin

fun initKoin() {
    startKoin {
        modules(authModule, analyticsModule)
    }
}
