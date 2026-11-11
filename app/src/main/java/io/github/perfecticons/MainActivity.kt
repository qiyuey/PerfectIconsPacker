package io.github.perfecticons

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.view.WindowCompat
import io.github.perfecticons.ui.PerfectIconsApp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.enableEdgeToEdge(window)
        window.isNavigationBarContrastEnforced = false
        setContent {
            PerfectIconsApp()
        }
    }
}
