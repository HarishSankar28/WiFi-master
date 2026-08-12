package com.wifisense.motiontracker

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Application class for the WiFi Motion Tracker app.
 * Annotated with @HiltAndroidApp to trigger Hilt's code generation
 * and serve as the application-level dependency injector.
 */
@HiltAndroidApp
class MotionTrackerApp : Application() {

    override fun onCreate() {
        super.onCreate()
        // Application-level initialization can go here
    }
}
