package com.wifisense.motiontracker.data.db

import com.wifisense.motiontracker.domain.model.ActivitySession
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository that abstracts Room database operations for [ActivitySession].
 * Converts between domain models and Room entities.
 */
@Singleton
class SessionRepository @Inject constructor(
    private val sessionDao: SessionDao
) {
    /** Observe all sessions, newest first */
    fun getAllSessions(): Flow<List<ActivitySession>> =
        sessionDao.getAllSessions().map { list -> list.map { it.toDomain() } }

    /** Observe the N most recent sessions */
    fun getRecentSessions(limit: Int = 20): Flow<List<ActivitySession>> =
        sessionDao.getRecentSessions(limit).map { list -> list.map { it.toDomain() } }

    /** Observe sessions within a time range (epoch ms) */
    fun getSessionsInRange(startMs: Long, endMs: Long): Flow<List<ActivitySession>> =
        sessionDao.getSessionsInRange(startMs, endMs).map { list -> list.map { it.toDomain() } }

    /** Insert a new session and return its database ID */
    suspend fun insertSession(session: ActivitySession): Long =
        sessionDao.insertSession(ActivitySessionEntity.fromDomain(session))

    /** Update an existing session */
    suspend fun updateSession(session: ActivitySession) =
        sessionDao.updateSession(ActivitySessionEntity.fromDomain(session))

    /** Delete a specific session by ID */
    suspend fun deleteSession(id: Long) = sessionDao.deleteSessionById(id)

    /** Delete all sessions */
    suspend fun deleteAllSessions() = sessionDao.deleteAllSessions()

    /** Total number of sessions recorded */
    suspend fun getTotalSessionCount(): Int = sessionDao.getTotalSessionCount()

    /** Total active minutes across all sessions */
    suspend fun getTotalActiveMinutes(): Int = sessionDao.getTotalActiveMinutes() ?: 0

    /** Total stationary minutes across all sessions */
    suspend fun getTotalStationaryMinutes(): Int = sessionDao.getTotalStationaryMinutes() ?: 0
}
