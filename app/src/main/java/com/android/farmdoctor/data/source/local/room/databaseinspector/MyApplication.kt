package com.android.farmdoctor.data.source.local.room.databaseinspector

import android.app.Application
import com.facebook.stetho.Stetho

class MyApplication: Application() {

    override fun onCreate() {
        super.onCreate()
        Stetho.initializeWithDefaults(this)
    }
}