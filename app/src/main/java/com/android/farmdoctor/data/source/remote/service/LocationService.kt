package com.android.farmdoctor.data.source.remote.service

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class LocationService(
    val latitude: String?,
    val longitude: String?
) : Parcelable