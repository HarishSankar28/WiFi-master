package com.wifisense.motiontracker.ui.screen.dashboard

import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wifisense.motiontracker.domain.model.MotionEvent
import com.wifisense.motiontracker.domain.model.MotionState
import com.wifisense.motiontracker.domain.usecase.StartMonitoringUseCase
import com.wifisense.motiontracker.domain.usecase.StopMonitoringUseCase
import com.wifisense.motiontracker.service.MotionSensingService
import com.wifisense.motiontracker.util.Constants
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DashboardUiState(
    val isMonitoring: Boolean = false,
    val isWifiEnabled: Boolean = true,
    val motionState: MotionState = MotionState.STATIONARY,
    val smoothedRssi: Float = -70f,
    val rawRssi: Int = -70,
    val rssiVariance: Double = 0.0,
    val routerSsid: String = "",
    val routerBssid: String = "",
    val sessionElapsedMs: Long = 0L,
    val rssiHistory: List<Float> = emptyList(),    // last 60 readings
    val errorMessage: String? = null
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val startMonitoringUseCase: StartMonitoringUseCase,
    private val stopMonitoringUseCase: StopMonitoringUseCase,
    private val dataStore: DataStore<androidx.datastore.preferences.core.Preferences>
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    private var monitoringJob: Job? = null
    private var sessionStartTime = 0L
    private var timerJob: Job? = null

    // DataStore keys
    private val keyBssid       = stringPreferencesKey(Constants.PREF_PINNED_BSSID)
    private val keyBaseline    = floatPreferencesKey(Constants.PREF_CALIBRATION_BASELINE)
    private val keySensitivity = intPreferencesKey(Constants.PREF_SENSITIVITY)

    fun toggleMonitoring() {
        if (_uiState.value.isMonitoring) stopMonitoring() else startMonitoring()
    }

    private fun startMonitoring() {
        viewModelScope.launch {
            val prefs = dataStore.data.first()
            val bssid = prefs[keyBssid]
            val baseline = prefs[keyBaseline]?.toDouble() ?: 0.0
            val sensitivity = prefs[keySensitivity] ?: 3

            // Start foreground service for background persistence
            ContextCompat.startForegroundService(
                context,
                MotionSensingService.startIntent(context, bssid, baseline, sensitivity)
            )

            sessionStartTime = System.currentTimeMillis()
            _uiState.update { it.copy(isMonitoring = true, errorMessage = null) }

            // Start session timer
            timerJob = viewModelScope.launch {
                while (true) {
                    _uiState.update { state ->
                        state.copy(sessionElapsedMs = System.currentTimeMillis() - sessionStartTime)
                    }
                    kotlinx.coroutines.delay(1000)
                }
            }

            // Collect motion events
            monitoringJob = viewModelScope.launch {
                startMonitoringUseCase(
                    targetBssid = bssid,
                    baselineVariance = baseline,
                    sensitivity = sensitivity
                ).catch { e ->
                    _uiState.update { it.copy(errorMessage = e.message) }
                }.collect { event ->
                    onMotionEvent(event)
                }
            }
        }
    }

    private fun stopMonitoring() {
        monitoringJob?.cancel()
        timerJob?.cancel()
        stopMonitoringUseCase()
        context.startService(MotionSensingService.stopIntent(context))
        _uiState.update { it.copy(isMonitoring = false) }
    }

    private fun onMotionEvent(event: MotionEvent) {
        _uiState.update { state ->
            val newHistory = (state.rssiHistory + event.smoothedRssi).takeLast(60)
            state.copy(
                motionState   = event.motionState,
                smoothedRssi  = event.smoothedRssi,
                rawRssi       = event.rawRssi,
                rssiVariance  = event.rssiVariance,
                routerSsid    = event.routerSsid,
                routerBssid   = event.routerBssid,
                rssiHistory   = newHistory
            )
        }
    }

    override fun onCleared() {
        super.onCleared()
        monitoringJob?.cancel()
        timerJob?.cancel()
    }
}
