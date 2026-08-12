package com.wifisense.motiontracker.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.wifisense.motiontracker.domain.model.ActivitySession
import com.wifisense.motiontracker.domain.model.MotionState

/**
 * Room database entity representing a completed motion sensing session.
 */
@Entity(tableName = "activity_sessions")
data class ActivitySessionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val startTime: Long,
    val endTime: Long,
    val routerSsid: String,
    val routerBssid: String,
    val dominantStateName: String,    // MotionState.name stored as String
    val averageVariance: Double,
    val peakVariance: Double,
    val stationaryMinutes: Int,
    val activeMinutes: Int,
    val eventCount: Int
) {
    fun toDomain(): ActivitySession = ActivitySession(
        id = id,
        startTime = startTime,
        endTime = endTime,
        routerSsid = routerSsid,
        routerBssid = routerBssid,
        dominantState = runCatching {
            MotionState.valueOf(dominantStateName)
        }.getOrDefault(MotionState.STATIONARY),
        averageVariance = averageVariance,
        peakVariance = peakVariance,
        stationaryMinutes = stationaryMinutes,
        activeMinutes = activeMinutes,
        eventCount = eventCount
    )

    companion object {
        fun fromDomain(session: ActivitySession) = ActivitySessionEntity(
            id = session.id,
            startTime = session.startTime,
            endTime = session.endTime,
            routerSsid = session.routerSsid,
            routerBssid = session.routerBssid,
            dominantStateName = session.dominantState.name,
            averageVariance = session.averageVariance,
            peakVariance = session.peakVariance,
            stationaryMinutes = session.stationaryMinutes,
            activeMinutes = session.activeMinutes,
            eventCount = session.eventCount
        )
    }
}
