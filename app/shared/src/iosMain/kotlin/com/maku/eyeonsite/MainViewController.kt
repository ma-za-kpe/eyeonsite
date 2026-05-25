package com.maku.eyeonsite

import androidx.compose.ui.window.ComposeUIViewController
import com.maku.eyeonsite.di.initKoin

private val koin by lazy { initKoin() }

fun mainViewController() = ComposeUIViewController { App() }.also { koin }
