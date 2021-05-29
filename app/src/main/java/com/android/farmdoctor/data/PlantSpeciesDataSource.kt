package com.android.farmdoctor.data

import androidx.lifecycle.LiveData
import com.android.farmdoctor.data.source.local.entity.MetaEntity
import com.android.farmdoctor.data.source.local.entity.PlantSpeciesEntity
import com.android.farmdoctor.data.source.local.entity.PlantSpeciesWithSynonym
import com.android.farmdoctor.data.source.remote.ApiResponse
import com.android.farmdoctor.data.source.remote.service.LocationService
import com.android.farmdoctor.vo.Resource

interface PlantSpeciesDataSource {

    fun getPlantSpecies(searchQuery: String, page: Int, order: Map<String, String>):
        LiveData<ApiResponse<List<PlantSpeciesEntity>>>
    fun getMeta(searchQuery: String, page: Int, order: Map<String, String>): LiveData<ApiResponse<MetaEntity>>
    fun getPlantSpeciesWithSynonyms(scientificName: String): LiveData<Resource<PlantSpeciesWithSynonym>>
}