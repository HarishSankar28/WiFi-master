package com.wifisense.motiontracker.domain.usecase

import com.wifisense.motiontracker.data.db.SessionRepository
import com.wifisense.motiontracker.domain.model.ActivitySession
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case that retrieves the list of past activity sessions from the database.
 */
class GetSessionHistoryUseCase @Inject constructor(
    private val sessionRepository: SessionRepository
) {
    /** Observe all sessions, newest first */
    operator fun invoke(): Flow<List<ActivitySession>> =
        sessionRepository.getAllSessions()

    /** Observe only the most recent N sessions */
    fun recent(limit: Int = 20): Flow<List<ActivitySession>> =
        sessionRepository.getRecentSessions(limit)

    /** Observe sessions within a time range */
    fun inRange(startMs: Long, endMs: Long): Flow<List<ActivitySession>> =
        sessionRepository.getSessionsInRange(startMs, endMs)
}
