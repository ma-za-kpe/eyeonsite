package com.maku.eyeonsite.ui.theme

import androidx.compose.runtime.compositionLocalOf

enum class AppVariant { CLIENT, INSPECTOR }

val LocalAppVariant = compositionLocalOf { AppVariant.CLIENT }
