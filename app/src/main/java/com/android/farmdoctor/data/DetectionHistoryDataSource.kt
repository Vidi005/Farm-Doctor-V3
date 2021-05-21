package com.android.farmdoctor.data

import androidx.lifecycle.LiveData
import androidx.paging.PagedList
import com.android.farmdoctor.data.source.local.entity.DetectionHistoryEntity
import com.android.farmdoctor.data.source.remote.service.LocationAddress
import com.android.farmdoctor.vo.Resource

interface DetectionHistoryDataSource {

    fun getDetectionHistories(filter: String): LiveData<Resource<PagedList<DetectionHistoryEntity>>>
    fun getSearchDetectionHistories(searchQuery: String): LiveData<Resource<PagedList<DetectionHistoryEntity>>>
    fun deleteAllDetectionHistories(detectionHistories: List<DetectionHistoryEntity>)
    fun getDetailDetectionHistoryById(id: Int): LiveData<Resource<DetectionHistoryEntity>>
    suspend fun deleteDetectionHistory(detectionHistory: DetectionHistoryEntity)
    suspend fun addAllDetectionHistories(detectionHistories: List<DetectionHistoryEntity>)
    suspend fun addToDetectionHistory(detectionHistory: DetectionHistoryEntity)
    fun geocodeLocationAddress(latitude: Double, longitude: Double)
    fun getLocationAddress(): LiveData<Resource<LocationAddress>>
}