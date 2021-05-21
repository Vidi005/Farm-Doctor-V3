package com.android.farmdoctor.helper

import androidx.sqlite.db.SimpleSQLiteQuery

object FilterUtils {

    const val NAME_ASC = "name_asc"
    const val NAME_DESC = "name_desc"
    const val DATE_ASC = "date_asc"
    const val DATE_DESC = "date_desc"
    const val CAMERA_ONLY = "camera_only"
    const val FILE_ONLY = "file_only"

    fun getDetectionHistoriesFilteredQuery(filter: String): SimpleSQLiteQuery {
        val simpleQuery = StringBuilder().append("SELECT * FROM DetectionHistoryEntity ")
        when (filter) {
            NAME_ASC -> simpleQuery.append("ORDER BY name ASC")
            NAME_DESC -> simpleQuery.append("ORDER BY name DESC")
            DATE_ASC -> simpleQuery.append("ORDER BY date ASC")
            DATE_DESC -> simpleQuery.append("ORDER BY date DESC")
            CAMERA_ONLY -> simpleQuery.append("WHERE detectionBased = \"Camera\" ORDER BY name ASC")
            FILE_ONLY -> simpleQuery.append("WHERE detectionBased = \"Gallery\" ORDER BY name ASC")
        }
        return SimpleSQLiteQuery("$simpleQuery")
    }
}