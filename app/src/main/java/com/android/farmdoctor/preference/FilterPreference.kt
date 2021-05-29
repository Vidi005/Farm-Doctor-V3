package com.android.farmdoctor.preference

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.android.farmdoctor.helper.FilterUtils.NAME_ASC

internal class FilterPreference(private val context: Context) {

    private lateinit var preferences: SharedPreferences
    companion object {
        private const val PREFS_FILTER = "filter_by"
    }

    fun setFilterBy(filter: String) {
        preferences =
            context.getSharedPreferences(PREFS_FILTER, Context.MODE_PRIVATE) as SharedPreferences
        preferences.edit { putString(PREFS_FILTER, filter) }
    }

    fun getFilterBy(): String {
        preferences =
            context.getSharedPreferences(PREFS_FILTER, Context.MODE_PRIVATE) as SharedPreferences
        return preferences.getString(PREFS_FILTER, NAME_ASC) as String
    }
}