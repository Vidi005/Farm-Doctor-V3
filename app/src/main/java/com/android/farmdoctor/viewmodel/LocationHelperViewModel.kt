package com.android.farmdoctor.viewmodel

import androidx.lifecycle.ViewModel
import com.android.farmdoctor.data.PlantDiseasesRepository
import com.android.farmdoctor.data.source.remote.service.LocationService

class LocationHelperViewModel(private val plantDiseasesRepository: PlantDiseasesRepository) :
    ViewModel() {

    fun setLastLocation(): LocationService = plantDiseasesRepository.setLastLocation()
    fun getLastLocation() = plantDiseasesRepository.getLastLocation()
}