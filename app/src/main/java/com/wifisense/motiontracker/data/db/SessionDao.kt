package com.wifisense.motiontracker.data.db

import androidx.room.*
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for [ActivitySessionEntity].
 * All database operations run on background threads via coroutines.
 */
@Dao
interface SessionDao {

    @Query("SELECT * FROM activity_sessions ORDER BY startTime DESC")
    fun getAllSessions(): Flow<List<ActivitySessionEntity>>

    @Query("SELECT * FROM activity_sessions ORDER BY startTime DESC LIMIT :limit")
    fun getRecentSessions(limit: Int): Flow<List<ActivitySessionEntity>>

    @Query("""
        SELECT * FROM activity_sessions
        WHERE startTime >= :startMs AND endTime <= :endMs
        ORDER BY startTime DESC
    """)
    fun getSessionsInRange(startMs: Long, endMs: Long): Flow<List<ActivitySessionEntity>>

    @Query("SELECT * FROM activity_sessions WHERE id = :id")
    suspend fun getSessionById(id: Long): ActivitySessionEntity?

    @Query("SELECT COUNT(*) FROM activity_sessions")
    suspend fun getTotalSessionCount(): Int

    @Query("SELECT SUM(activeMinutes) FROM activity_sessions")
    suspend fun getTotalActiveMinutes(): Int?

    @Query("SELECT SUM(stationaryMinutes) FROM activity_sessions")
    suspend fun getTotalStationaryMinutes(): Int?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: ActivitySessionEntity): Long

    @Update
    suspend fun updateSession(session: ActivitySessionEntity)

    @Delete
    suspend fun deleteSession(session: ActivitySessionEntity)

    @Query("DELETE FROM activity_sessions WHERE id = :id")
    suspend fun deleteSessionById(id: Long)

    @Query("DELETE FROM activity_sessions")
    suspend fun deleteAllSessions()
}
