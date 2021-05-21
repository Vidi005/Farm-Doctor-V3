package com.android.farmdoctor.preference

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.android.farmdoctor.ui.detection.CameraFragment.Companion.flashModes

internal class CameraPreference {

    private lateinit var preferences: SharedPreferences
    companion object {
        private const val PREFS_AUTO_DETECT = "auto_detect_pref"
        private const val PREFS_FLASHLIGHT_MODE = "flashlight_mode_pref"
        private const val PREFS_ENABLE_LOCATION = "enable_location_pref"
        private const val PREFS_DETECTION_HISTORY = "detection_history_pref"
    }

    fun setAutoDetect(context: Context) {
        preferences = context.getSharedPreferences(PREFS_AUTO_DETECT, Context.MODE_PRIVATE) as SharedPreferences
        preferences.edit { putBoolean(PREFS_AUTO_DETECT, true) }
    }

    fun getAutoDetect(context: Context): Boolean {
        preferences = context.getSharedPreferences(PREFS_AUTO_DETECT, Context.MODE_PRIVATE) as SharedPreferences
        return preferences.getBoolean(PREFS_AUTO_DETECT, false)
    }

    fun unsetAutoDetect(context: Context) {
        preferences = context.getSharedPreferences(PREFS_AUTO_DETECT, Context.MODE_PRIVATE) as SharedPreferences
        preferences.edit { clear() }
    }

    fun setFlashLightMode(context: Context) {
        preferences = context.getSharedPreferences(PREFS_FLASHLIGHT_MODE, Context.MODE_PRIVATE) as SharedPreferences
        preferences.edit { putInt(PREFS_FLASHLIGHT_MODE, flashModes) }
    }

    fun getFlashLightMode(context: Context): Int {
        preferences = context.getSharedPreferences(PREFS_FLASHLIGHT_MODE, Context.MODE_PRIVATE) as SharedPreferences
        return preferences.getInt(PREFS_FLASHLIGHT_MODE, 1)
    }

    fun setLocation(context: Context) {
        preferences = context.getSharedPreferences(PREFS_ENABLE_LOCATION, Context.MODE_PRIVATE) as SharedPreferences
        preferences.edit { putBoolean(PREFS_ENABLE_LOCATION, true) }
    }

    fun getLocation(context: Context): Boolean {
        preferences = context.getSharedPreferences(PREFS_ENABLE_LOCATION, Context.MODE_PRIVATE) as SharedPreferences
        return preferences.getBoolean(PREFS_ENABLE_LOCATION, false)
    }

    fun unsetLocation(context: Context) {
        preferences = context.getSharedPreferences(PREFS_ENABLE_LOCATION, Context.MODE_PRIVATE) as SharedPreferences
        preferences.edit { clear() }
    }

    fun setDetectionHistory(context: Context) {
        preferences = context.getSharedPreferences(PREFS_DETECTION_HISTORY, Context.MODE_PRIVATE) as SharedPreferences
        preferences.edit { putBoolean(PREFS_DETECTION_HISTORY, true) }
    }

    fun getDetectionHistory(context: Context): Boolean {
        preferences = context.getSharedPreferences(PREFS_DETECTION_HISTORY, Context.MODE_PRIVATE) as SharedPreferences
        return preferences.getBoolean(PREFS_DETECTION_HISTORY, false)
    }

    fun unsetDetectionHistory(context: Context) {
        preferences = context.getSharedPreferences(PREFS_DETECTION_HISTORY, Context.MODE_PRIVATE) as SharedPreferences
        preferences.edit { clear() }
    }
}