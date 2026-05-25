package com.maku.eyeonsite.di

import com.maku.eyeonsite.ui.auth.AuthViewModel
import com.maku.eyeonsite.ui.auth.FirebaseAuthProvider
import com.maku.eyeonsite.ui.auth.createFirebaseAuthProvider
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val authModule =
    module {
        factory<FirebaseAuthProvider> { createFirebaseAuthProvider() }
        viewModelOf(::AuthViewModel)
    }

expect val analyticsModule: Module
