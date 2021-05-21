package com.android.farmdoctor.viewmodel

import androidx.lifecycle.ViewModel
import com.android.farmdoctor.data.PlantDiseasesRepository
import com.android.farmdoctor.data.source.factory.model.PlantDisease

class PlantDiseaseViewModel(private val plantDiseasesRepository: PlantDiseasesRepository) : ViewModel() {

    fun getPlantDiseases(): ArrayList<PlantDisease> = plantDiseasesRepository.getPlantDiseases()
}