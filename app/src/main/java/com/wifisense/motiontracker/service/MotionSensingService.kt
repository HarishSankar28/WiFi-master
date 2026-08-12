package com.wifisense.motiontracker.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.wifisense.motiontracker.MainActivity
import com.wifisense.motiontracker.R
import com.wifisense.motiontracker.data.db.SessionRepository
import com.wifisense.motiontracker.data.processing.SignalProcessor
import com.wifisense.motiontracker.domain.model.ActivitySession
import com.wifisense.motiontracker.domain.model.MotionEvent
import com.wifisense.motiontracker.domain.model.MotionState
import com.wifisense.motiontracker.domain.usecase.StartMonitoringUseCase
import com.wifisense.motiontracker.util.Constants
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import javax.inject.Inject

/**
 * Foreground Service that continuously monitors WiFi signals for human motion.
 * Persists across app backgrounding. Shows a live notification with current state.
 *
 * Lifecycle:
 *   startForegroundService() → onCreate() → startForeground() → monitoring loop
 *   stopSelf() / stopService() → onDestroy() → saves session to DB
 */
@AndroidEntryPoint
class MotionSensingService : Service() {

    @Inject lateinit var startMonitoringUseCase: StartMonitoringUseCase
    @Inject lateinit var sessionRepository: SessionRepository
    @Inject lateinit var signalProcessor: SignalProcessor

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var monitoringJob: Job? = null

    // Session tracking
    private var sessionStartTime: Long = 0L
    private var stationaryMinutes = 0
    private var activeMinutes = 0
    private var eventCount = 0
    private var totalVariance = 0.0
    private var peakVariance = 0.0
    private var lastRouterSsid = ""
    private var lastRouterBssid = ""
    private val stateCounts = mutableMapOf<MotionState, Int>()

    // Binder for Activity binding
    private val binder = LocalBinder()

    // Expose current motion events to bound clients
    private val _motionEvents = MutableSharedFlow<MotionEvent>(replay = 1)
    val motionEvents: SharedFlow<MotionEvent> = _motionEvents.asSharedFlow()

    inner class LocalBinder : Binder() {
        fun getService(): MotionSensingService = this@MotionSensingService
    }

    override fun onBind(intent: Intent): IBinder = binder

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        startForeground(Constants.NOTIFICATION_ID, buildNotification("Initializing…"))
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> startMonitoring(
                bssid = intent.getStringExtra(EXTRA_BSSID),
                baseline = intent.getDoubleExtra(EXTRA_BASELINE, 0.0),
                sensitivity = intent.getIntExtra(EXTRA_SENSITIVITY, 3)
            )
            ACTION_STOP -> stopMonitoringAndSave()
        }
        return START_STICKY
    }

    private fun startMonitoring(bssid: String?, baseline: Double, sensitivity: Int) {
        sessionStartTime = System.currentTimeMillis()
        stateCounts.clear()

        monitoringJob = serviceScope.launch {
            startMonitoringUseCase(
                targetBssid = bssid,
                baselineVariance = baseline,
                sensitivity = sensitivity
            ).collect { event ->
                processEvent(event)
                _motionEvents.emit(event)
                updateNotification(event)
            }
        }
    }

    private fun processEvent(event: MotionEvent) {
        eventCount++
        totalVariance += event.rssiVariance
        if (event.rssiVariance > peakVariance) peakVariance = event.rssiVariance
        lastRouterSsid = event.routerSsid
        lastRouterBssid = event.routerBssid
        stateCounts[event.motionState] = (stateCounts[event.motionState] ?: 0) + 1

        // Approximate minutes (each event ≈ 2 seconds → 30 events per minute)
        if (event.motionState == MotionState.STATIONARY) stationaryMinutes++
        else activeMinutes++
    }

    private fun stopMonitoringAndSave() {
        monitoringJob?.cancel()
        if (sessionStartTime > 0 && eventCount > 0) {
            serviceScope.launch {
                val dominantState = stateCounts.maxByOrNull { it.value }?.key
                    ?: MotionState.STATIONARY
                sessionRepository.insertSession(
                    ActivitySession(
                        startTime = sessionStartTime,
                        endTime = System.currentTimeMillis(),
                        routerSsid = lastRouterSsid,
                        routerBssid = lastRouterBssid,
                        dominantState = dominantState,
                        averageVariance = if (eventCount > 0) totalVariance / eventCount else 0.0,
                        peakVariance = peakVariance,
                        stationaryMinutes = stationaryMinutes / 30,
                        activeMinutes = activeMinutes / 30,
                        eventCount = eventCount
                    )
                )
                stopSelf()
            }
        } else {
            stopSelf()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }

    // ─── Notification ──────────────────────────────────────────────────────────

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            Constants.NOTIFICATION_CHANNEL_ID,
            Constants.NOTIFICATION_CHANNEL_NAME,
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Shows live WiFi motion sensing status"
            setShowBadge(false)
        }
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(channel)
    }

    private fun buildNotification(status: String) = NotificationCompat.Builder(
        this, Constants.NOTIFICATION_CHANNEL_ID
    )
        .setContentTitle("WiFi Motion Tracker")
        .setContentText(status)
        .setSmallIcon(android.R.drawable.ic_menu_compass)
        .setOngoing(true)
        .setSilent(true)
        .setContentIntent(
            PendingIntent.getActivity(
                this, 0,
                Intent(this, MainActivity::class.java),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        )
        .build()

    private fun updateNotification(event: MotionEvent) {
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(
            Constants.NOTIFICATION_ID,
            buildNotification("${event.motionState.label} — ${event.routerSsid}")
        )
    }

    companion object {
        const val ACTION_START = "com.wifisense.ACTION_START"
        const val ACTION_STOP  = "com.wifisense.ACTION_STOP"
        const val EXTRA_BSSID = "extra_bssid"
        const val EXTRA_BASELINE = "extra_baseline"
        const val EXTRA_SENSITIVITY = "extra_sensitivity"

        fun startIntent(context: Context, bssid: String?, baseline: Double, sensitivity: Int) =
            Intent(context, MotionSensingService::class.java).apply {
                action = ACTION_START
                putExtra(EXTRA_BSSID, bssid)
                putExtra(EXTRA_BASELINE, baseline)
                putExtra(EXTRA_SENSITIVITY, sensitivity)
            }

        fun stopIntent(context: Context) =
            Intent(context, MotionSensingService::class.java).apply {
                action = ACTION_STOP
            }
    }
}
