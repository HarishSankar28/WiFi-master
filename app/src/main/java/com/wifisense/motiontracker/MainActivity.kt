package com.wifisense.motiontracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.wifisense.motiontracker.ui.navigation.NavGraph
import com.wifisense.motiontracker.ui.theme.WiFiMotionTrackerTheme
import dagger.hilt.android.AndroidEntryPoint

/**
 * Main entry Activity for the WiFi Motion Tracker application.
 * Sets up the Compose UI and navigation graph.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            WiFiMotionTrackerTheme {
                Surface(
                    modifier = Modifier.fillMaxSize()
                ) {
                    NavGraph()
                }
            }
        }
    }
}
