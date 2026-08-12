package com.wifisense.motiontracker.di

import com.wifisense.motiontracker.data.processing.SignalProcessor
import com.wifisense.motiontracker.data.wifi.WifiScanRepository
import com.wifisense.motiontracker.data.wifi.WifiScanner
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object WifiModule {

    @Provides
    @Singleton
    fun provideSignalProcessor(): SignalProcessor = SignalProcessor()
}
