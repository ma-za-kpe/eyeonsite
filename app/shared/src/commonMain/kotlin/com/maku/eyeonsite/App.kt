package com.maku.eyeonsite

import androidx.compose.runtime.Composable
import com.maku.eyeonsite.ui.auth.SignInScreen
import com.maku.eyeonsite.ui.theme.EyeOnSiteTheme

@Composable
fun App() {
    EyeOnSiteTheme {
        SignInScreen()
    }
}
