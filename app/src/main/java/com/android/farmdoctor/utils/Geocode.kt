package com.android.farmdoctor.utils

import android.app.Activity
import android.location.Geocoder
import com.android.farmdoctor.data.source.remote.service.LocationAddress
import java.util.*

class Geocode(private val activity: Activity) {

    fun setLocationAddress(latitude: Double, longitude: Double): LocationAddress {
        val locationAddress: LocationAddress?
        val geocode = Geocoder(activity, Locale.getDefault())
        val addresses = geocode.getFromLocation(latitude, longitude, 1)
        locationAddress = if (!addresses.isNullOrEmpty() && addresses.size > 0) {
            val address = addresses[0].getAddressLine(0)
            LocationAddress(address)
        } else LocationAddress("")
        return locationAddress
    }
}