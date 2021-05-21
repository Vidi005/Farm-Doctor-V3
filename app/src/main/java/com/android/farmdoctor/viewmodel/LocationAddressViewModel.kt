package com.android.farmdoctor.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.android.farmdoctor.data.DetectionHistoryRepository
import com.android.farmdoctor.data.source.remote.service.LocationAddress
import com.android.farmdoctor.vo.Resource

class LocationAddressViewModel(private val detectionHistoryRepository: DetectionHistoryRepository) :
    ViewModel() {

    fun geocodeLocationAddress(latitude: Double, longitude: Double) =
        detectionHistoryRepository.geocodeLocationAddress(latitude, longitude)

    fun getLocationAddress(): LiveData<Resource<LocationAddress>> =
        detectionHistoryRepository.getLocationAddress()
}