package com.maku.eyeonsite

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.maku.eyeonsite.analytics.FirebaseAnalyticsService
import com.maku.eyeonsite.ui.auth.ActivityHolder

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        ActivityHolder.activity = this
        setContent {
            App(analytics = FirebaseAnalyticsService(this))
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (ActivityHolder.activity === this) ActivityHolder.activity = null
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}
