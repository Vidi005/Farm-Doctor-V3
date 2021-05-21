package com.android.farmdoctor.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.android.farmdoctor.data.PlantSpeciesRepository
import com.android.farmdoctor.data.source.local.entity.ItemsCountEntity
import com.android.farmdoctor.data.source.local.entity.MetaEntity
import com.android.farmdoctor.data.source.local.entity.PlantSpeciesEntity
import com.android.farmdoctor.data.source.remote.ApiResponse

class PlantSpeciesViewModel(private val plantSpeciesRepository: PlantSpeciesRepository) :
    ViewModel() {

    private val _itemsCount = MutableLiveData<ItemsCountEntity>()
    val itemCounts: LiveData<ItemsCountEntity> get() = _itemsCount

    fun getPlantSpecies(searchQuery: String, page: Int, order: Map<String, String>): LiveData<ApiResponse<List<PlantSpeciesEntity>>> =
        plantSpeciesRepository.getPlantSpecies(searchQuery, page, order)

    fun getMeta(searchQuery: String, page: Int, order: Map<String, String>): LiveData<ApiResponse<MetaEntity>> =
        plantSpeciesRepository.getMeta(searchQuery, page, order)

    fun getItemCounts(itemsCount: ItemsCountEntity) {
        _itemsCount.value = itemsCount
    }
}