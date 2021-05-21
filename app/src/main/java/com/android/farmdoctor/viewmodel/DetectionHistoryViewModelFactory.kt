package com.android.farmdoctor.viewmodel

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.android.farmdoctor.data.DetectionHistoryRepository
import com.android.farmdoctor.di.Injection.provideDetectionHistoryRepository

class DetectionHistoryViewModelFactory private constructor(
    private val mDetectionHistoryRepository: DetectionHistoryRepository) :
    ViewModelProvider.NewInstanceFactory() {

    companion object {
        @Volatile
        private var INSTANCE: DetectionHistoryViewModelFactory? = null

        fun getInstance(activity: Activity): DetectionHistoryViewModelFactory =
            INSTANCE ?: synchronized(this) {
                DetectionHistoryViewModelFactory(provideDetectionHistoryRepository(activity)).apply {
                    INSTANCE = this
                }
            }
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T = when {
        modelClass.isAssignableFrom(DetectionHistoryViewModel::class.java) ->
            DetectionHistoryViewModel(mDetectionHistoryRepository) as T
        modelClass.isAssignableFrom(DetailDetectionHistoryViewModel::class.java) ->
            DetailDetectionHistoryViewModel(mDetectionHistoryRepository) as T
        modelClass.isAssignableFrom(LocationAddressViewModel::class.java) ->
            LocationAddressViewModel(mDetectionHistoryRepository) as T
        else -> throw Throwable("Unknown ViewModel class: ${modelClass.name}")
    }
}