package com.android.farmdoctor.manager

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

object PermissionManager {

    fun check(context: Context, activity: Activity, permission: String, requestCode: Int) {
        if (ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(activity, arrayOf(permission), requestCode)
    }
}