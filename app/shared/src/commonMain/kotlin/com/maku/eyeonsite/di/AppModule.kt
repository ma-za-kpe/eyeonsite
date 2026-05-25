package com.maku.eyeonsite.di

import com.maku.eyeonsite.ui.auth.AuthViewModel
import com.maku.eyeonsite.ui.auth.FirebaseAuthProvider
import com.maku.eyeonsite.ui.auth.createFirebaseAuthProvider
import com.maku.eyeonsite.ui.settings.SettingsViewModel
import org.koin.core.module.Module
import org.koin.dsl.module

val authModule =
    module {
        factory<FirebaseAuthProvider> { createFirebaseAuthProvider() }
        factory { AuthViewModel(get(), get()) }
        factory { SettingsViewModel() }
    }

expect val analyticsModule: Module
