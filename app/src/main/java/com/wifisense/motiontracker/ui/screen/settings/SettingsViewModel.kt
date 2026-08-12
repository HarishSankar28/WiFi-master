package com.wifisense.motiontracker.ui.screen.settings

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wifisense.motiontracker.util.Constants
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val pinnedSsid: String = "",
    val pinnedBssid: String = "",
    val sensitivityLevel: Int = 3,
    val backgroundEnabled: Boolean = false,
    val notificationsEnabled: Boolean = true,
    val calibrationDone: Boolean = false,
    val baselineVariance: Double = 0.0
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val dataStore: DataStore<androidx.datastore.preferences.core.Preferences>
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    private val keyBssid         = stringPreferencesKey(Constants.PREF_PINNED_BSSID)
    private val keySsid          = stringPreferencesKey(Constants.PREF_PINNED_SSID)
    private val keySensitivity   = intPreferencesKey(Constants.PREF_SENSITIVITY)
    private val keyBgEnabled     = booleanPreferencesKey(Constants.PREF_BACKGROUND_ENABLED)
    private val keyNotifEnabled  = booleanPreferencesKey(Constants.PREF_NOTIFICATIONS_ENABLED)
    private val keyCalibDone     = booleanPreferencesKey(Constants.PREF_CALIBRATION_DONE)
    private val keyBaseline      = floatPreferencesKey(Constants.PREF_CALIBRATION_BASELINE)

    init {
        viewModelScope.launch {
            dataStore.data.collect { prefs ->
                _uiState.update {
                    it.copy(
                        pinnedBssid        = prefs[keyBssid] ?: "",
                        pinnedSsid         = prefs[keySsid] ?: "",
                        sensitivityLevel   = prefs[keySensitivity] ?: 3,
                        backgroundEnabled  = prefs[keyBgEnabled] ?: false,
                        notificationsEnabled = prefs[keyNotifEnabled] ?: true,
                        calibrationDone    = prefs[keyCalibDone] ?: false,
                        baselineVariance   = (prefs[keyBaseline] ?: 0f).toDouble()
                    )
                }
            }
        }
    }

    fun setSensitivity(level: Int) {
        viewModelScope.launch {
            dataStore.edit { it[keySensitivity] = level }
        }
    }

    fun setBackgroundEnabled(enabled: Boolean) {
        viewModelScope.launch {
            dataStore.edit { it[keyBgEnabled] = enabled }
        }
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            dataStore.edit { it[keyNotifEnabled] = enabled }
        }
    }

    fun clearCalibration() {
        viewModelScope.launch {
            dataStore.edit {
                it[keyCalibDone] = false
                it[keyBaseline] = 0f
            }
        }
    }

    fun setPinnedRouter(ssid: String, bssid: String) {
        viewModelScope.launch {
            dataStore.edit {
                it[keySsid] = ssid
                it[keyBssid] = bssid
            }
        }
    }
}
