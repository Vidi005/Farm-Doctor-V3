package com.android.farmdoctor.viewmodel

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.android.farmdoctor.data.PlantSpeciesRepository
import com.android.farmdoctor.di.Injection.providePlantSpeciesRepository

class PlantSpeciesViewModelFactory private constructor(
    private val mPlantSpeciesRepository: PlantSpeciesRepository) :
        ViewModelProvider.NewInstanceFactory() {

        companion object {
            @Volatile
            private var INSTANCE: PlantSpeciesViewModelFactory? = null

            fun getInstance(activity: Activity): PlantSpeciesViewModelFactory =
                INSTANCE ?: synchronized(this) {
                    PlantSpeciesViewModelFactory(providePlantSpeciesRepository(activity)).apply {
                        INSTANCE = this
                    }
            }
        }

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T = when {
        modelClass.isAssignableFrom(PlantSpeciesViewModel::class.java) ->
            PlantSpeciesViewModel(mPlantSpeciesRepository) as T
        modelClass.isAssignableFrom(DetailPlantSpeciesViewModel::class.java) ->
            DetailPlantSpeciesViewModel(mPlantSpeciesRepository) as T
        else -> throw Throwable("Unknown ViewModel class: ${modelClass.name}")
    }
}