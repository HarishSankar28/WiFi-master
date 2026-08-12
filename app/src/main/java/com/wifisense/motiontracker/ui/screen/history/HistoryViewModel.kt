package com.wifisense.motiontracker.ui.screen.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wifisense.motiontracker.data.db.SessionRepository
import com.wifisense.motiontracker.domain.model.ActivitySession
import com.wifisense.motiontracker.domain.usecase.GetSessionHistoryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HistoryUiState(
    val sessions: List<ActivitySession> = emptyList(),
    val isLoading: Boolean = true,
    val totalActiveMinutes: Int = 0,
    val totalSessions: Int = 0
)

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val getSessionHistoryUseCase: GetSessionHistoryUseCase,
    private val sessionRepository: SessionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HistoryUiState())
    val uiState: StateFlow<HistoryUiState> = _uiState.asStateFlow()

    init {
        loadSessions()
    }

    private fun loadSessions() {
        viewModelScope.launch {
            getSessionHistoryUseCase().collect { sessions ->
                _uiState.update {
                    it.copy(
                        sessions = sessions,
                        isLoading = false,
                        totalSessions = sessions.size,
                        totalActiveMinutes = sessions.sumOf { s -> s.activeMinutes }
                    )
                }
            }
        }
    }

    fun deleteSession(session: ActivitySession) {
        viewModelScope.launch {
            sessionRepository.deleteSession(session.id)
        }
    }

    fun deleteAllSessions() {
        viewModelScope.launch {
            sessionRepository.deleteAllSessions()
        }
    }
}
