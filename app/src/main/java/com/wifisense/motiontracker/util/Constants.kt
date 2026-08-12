package com.wifisense.motiontracker.util

object Constants {

    // ─── Router / WiFi ──────────────────────────────────────────────────────
    /** Keyword used to auto-detect the UBIQCOM router SSID */
    const val ROUTER_SSID_KEYWORD = "UBIQCOM"

    /** Default router admin IP for UBIQCOM UB5021 GVWD */
    const val ROUTER_DEFAULT_IP = "192.168.1.1"
    const val ROUTER_FALLBACK_IP = "192.168.1.200"

    /** Default router web UI credentials */
    const val ROUTER_DEFAULT_USER = "user"
    const val ROUTER_DEFAULT_PASS = "user"

    // ─── Scanning ───────────────────────────────────────────────────────────
    /** Minimum interval between full WiFi scans (Android throttling: 4 per 2 min) */
    const val SCAN_INTERVAL_MS = 30_000L  // 30 seconds

    /** How often to poll connected-network RSSI (no throttle limit) */
    const val RSSI_POLL_INTERVAL_MS = 2_000L  // 2 seconds

    /** Number of samples to keep in the rolling window */
    const val ROLLING_WINDOW_SIZE = 20

    /** Moving-average filter window */
    const val MOVING_AVG_WINDOW = 5

    // ─── Motion Thresholds (RSSI variance in dBm²) ──────────────────────────
    const val THRESHOLD_MINOR_MOTION = 2.0
    const val THRESHOLD_MODERATE_MOTION = 8.0
    const val THRESHOLD_HEAVY_MOTION = 20.0

    // ─── Calibration ────────────────────────────────────────────────────────
    /** Duration of calibration phase in milliseconds */
    const val CALIBRATION_DURATION_MS = 30_000L

    /** Minimum samples needed to complete calibration */
    const val CALIBRATION_MIN_SAMPLES = 10

    // ─── Database ───────────────────────────────────────────────────────────
    const val DATABASE_NAME = "motion_tracker.db"

    // ─── DataStore Keys ─────────────────────────────────────────────────────
    const val PREFS_NAME = "motion_tracker_prefs"
    const val PREF_PINNED_BSSID = "pinned_bssid"
    const val PREF_PINNED_SSID = "pinned_ssid"
    const val PREF_SENSITIVITY = "sensitivity_level"  // 1–5
    const val PREF_BACKGROUND_ENABLED = "background_service_enabled"
    const val PREF_CALIBRATION_BASELINE = "calibration_baseline_variance"
    const val PREF_CALIBRATION_DONE = "calibration_done"
    const val PREF_NOTIFICATIONS_ENABLED = "notifications_enabled"

    // ─── Notification ───────────────────────────────────────────────────────
    const val NOTIFICATION_CHANNEL_ID = "motion_sensing_channel"
    const val NOTIFICATION_CHANNEL_NAME = "Motion Sensing"
    const val NOTIFICATION_ID = 1001

    // ─── Export ─────────────────────────────────────────────────────────────
    const val EXPORT_DIR = "WiFiMotionTracker"
    const val EXPORT_FILE_PREFIX = "session_export_"
}
