package com.android.farmdoctor.viewmodel

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.android.farmdoctor.data.PlantDiseasesRepository
import com.android.farmdoctor.di.Injection.providePlantDiseasesRepository

class PlantDiseaseViewModelFactory private constructor(
    private val mPlantDiseasesRepository: PlantDiseasesRepository) :
    ViewModelProvider.NewInstanceFactory() {

    companion object {
        @Volatile
        private var INSTANCE: PlantDiseaseViewModelFactory? = null

        fun getInstance(activity: Activity): PlantDiseaseViewModelFactory =
            INSTANCE ?: synchronized(this) {
                PlantDiseaseViewModelFactory(providePlantDiseasesRepository(activity)).apply {
                    INSTANCE = this
                }
            }
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T = when {
        modelClass.isAssignableFrom(PlantDiseaseViewModel::class.java) ->
            PlantDiseaseViewModel(mPlantDiseasesRepository) as T
        modelClass.isAssignableFrom(DetectionViewModel::class.java) ->
            DetectionViewModel(mPlantDiseasesRepository) as T
        modelClass.isAssignableFrom(LocationHelperViewModel::class.java) ->
            LocationHelperViewModel(mPlantDiseasesRepository) as T
        else -> throw Throwable("Unknown ViewModel class: ${modelClass.name}")
    }
}