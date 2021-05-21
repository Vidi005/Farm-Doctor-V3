package com.android.farmdoctor.utils

import android.annotation.SuppressLint
import android.app.Activity
import android.location.LocationManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class LocationHelper(private val activity: Activity) {

    var dataLatitude = ""
    var dataLongitude = ""

    @SuppressLint("MissingPermission")
    fun getLastLocation() {
        val locationManager = activity.getSystemService(Activity.LOCATION_SERVICE) as LocationManager
        GlobalScope.launch(Dispatchers.Default) {
            while (true) {
                val location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                if (location != null) {
                    dataLatitude = "${location.latitude}"
                    dataLongitude = "${location.longitude}"
                } else {
                    dataLatitude = ""
                    dataLongitude = ""
                }
                delay(1000L)
            }
        }
    }
}