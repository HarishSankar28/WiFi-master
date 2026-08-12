package com.wifisense.motiontracker.ui.screen.calibration

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wifisense.motiontracker.data.processing.SignalProcessor
import com.wifisense.motiontracker.data.wifi.WifiScanRepository
import com.wifisense.motiontracker.util.Constants
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CalibrationUiState(
    val isCalibrating: Boolean = false,
    val progress: Float = 0f,
    val remainingSeconds: Int = 30,
    val sampleCount: Int = 0,
    val computedBaseline: Double = 0.0,
    val isComplete: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class CalibrationViewModel @Inject constructor(
    private val wifiScanRepository: WifiScanRepository,
    private val signalProcessor: SignalProcessor,
    private val dataStore: DataStore<Preferences>
) : ViewModel() {

    private val _uiState = MutableStateFlow(CalibrationUiState())
    val uiState: StateFlow<CalibrationUiState> = _uiState.asStateFlow()

    private var calibrationJob: Job? = null
    private val collectedRssiList = mutableListOf<Int>()

    private val keyCalibDone = booleanPreferencesKey(Constants.PREF_CALIBRATION_DONE)
    private val keyBaseline  = floatPreferencesKey(Constants.PREF_CALIBRATION_BASELINE)

    fun startCalibration() {
        collectedRssiList.clear()
        _uiState.update {
            CalibrationUiState(
                isCalibrating = true,
                remainingSeconds = (Constants.CALIBRATION_DURATION_MS / 1000).toInt()
            )
        }

        calibrationJob = viewModelScope.launch {
            val startTime = System.currentTimeMillis()
            val duration = Constants.CALIBRATION_DURATION_MS

            launch {
                while (System.currentTimeMillis() - startTime < duration) {
                    val elapsed = System.currentTimeMillis() - startTime
                    val progress = (elapsed.toFloat() / duration).coerceIn(0f, 1f)
                    val remaining = ((duration - elapsed) / 1000).toInt().coerceAtLeast(0)
                    _uiState.update { it.copy(progress = progress, remainingSeconds = remaining) }
                    delay(500)
                }
            }

            wifiScanRepository.signalFlow().collect { sample ->
                collectedRssiList.add(sample.rssi)
                _uiState.update { it.copy(sampleCount = collectedRssiList.size) }

                if (System.currentTimeMillis() - startTime >= duration) {
                    finishCalibration()
                }
            }
        }
    }

    private suspend fun finishCalibration() {
        calibrationJob?.cancel()

        if (collectedRssiList.size < Constants.CALIBRATION_MIN_SAMPLES) {
            _uiState.update {
                it.copy(
                    isCalibrating = false,
                    errorMessage = "Not enough WiFi samples collected. Ensure WiFi is connected and try again."
                )
            }
            return
        }

        val baselineVariance = signalProcessor.computeVarianceOf(collectedRssiList)
        signalProcessor.setBaselineVariance(baselineVariance)

        dataStore.edit { prefs ->
            prefs[keyCalibDone] = true
            prefs[keyBaseline] = baselineVariance.toFloat()
        }

        _uiState.update {
            it.copy(
                isCalibrating = false,
                progress = 1f,
                remainingSeconds = 0,
                computedBaseline = baselineVariance,
                isComplete = true
            )
        }
    }

    override fun onCleared() {
        super.onCleared()
        calibrationJob?.cancel()
    }
}
