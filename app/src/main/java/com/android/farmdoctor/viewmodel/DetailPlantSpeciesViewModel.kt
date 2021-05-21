package com.android.farmdoctor.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.android.farmdoctor.data.PlantSpeciesRepository
import com.android.farmdoctor.data.source.local.entity.PlantSpeciesWithSynonym
import com.android.farmdoctor.vo.Resource

class DetailPlantSpeciesViewModel(private val mPlantSpeciesRepository: PlantSpeciesRepository) :
    ViewModel() {

    private var scientificName = MutableLiveData<String>()

    fun setSelectedPlantSpecies(sciName: String) {
        scientificName.value = sciName
    }

    var detailPlantSpecies: LiveData<Resource<PlantSpeciesWithSynonym>> =
        Transformations.switchMap(scientificName) {
            mPlantSpeciesRepository.getPlantSpeciesWithSynonyms(it)
    }
}