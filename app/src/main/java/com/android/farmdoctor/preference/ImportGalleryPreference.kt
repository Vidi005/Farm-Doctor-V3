package com.android.farmdoctor.preference

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

internal class ImportGalleryPreference {

    private lateinit var preferences: SharedPreferences
    companion object {
        private const val PREFS_GALLERY_DETECTION_HISTORY = "gallery_detection_history_pref"
    }

    fun setDetectionHistory(context: Context) {
        preferences = context.getSharedPreferences(PREFS_GALLERY_DETECTION_HISTORY, Context.MODE_PRIVATE) as SharedPreferences
        preferences.edit { putBoolean(PREFS_GALLERY_DETECTION_HISTORY, true) }
    }

    fun getDetectionHistory(context: Context): Boolean {
        preferences = context.getSharedPreferences(PREFS_GALLERY_DETECTION_HISTORY, Context.MODE_PRIVATE) as SharedPreferences
        return preferences.getBoolean(PREFS_GALLERY_DETECTION_HISTORY, false)
    }

    fun unsetDetectionHistory(context: Context) {
        preferences = context.getSharedPreferences(PREFS_GALLERY_DETECTION_HISTORY, Context.MODE_PRIVATE) as SharedPreferences
        preferences.edit { clear() }
    }
}