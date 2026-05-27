package com.maku.eyeonsite

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.maku.eyeonsite.ui.auth.ActivityHolder
import com.maku.eyeonsite.ui.theme.AppVariant

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        ActivityHolder.activity = this
        val forcedVariant =
            when (BuildConfig.FLAVOR) {
                "inspector" -> AppVariant.INSPECTOR
                "client" -> AppVariant.CLIENT
                else -> null
            }
        setContent { App(forcedVariant = forcedVariant) }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (ActivityHolder.activity === this) ActivityHolder.activity = null
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    com.maku.eyeonsite.ui.theme.EyeOnSiteTheme {
        com.maku.eyeonsite.ui.auth
            .SignInScreen()
    }
}
