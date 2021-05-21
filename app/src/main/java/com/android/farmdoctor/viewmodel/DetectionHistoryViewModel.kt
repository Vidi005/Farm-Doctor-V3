package com.android.farmdoctor.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagedList
import com.android.farmdoctor.data.DetectionHistoryRepository
import com.android.farmdoctor.data.source.local.entity.DetectionHistoryEntity
import com.android.farmdoctor.vo.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class DetectionHistoryViewModel(private val detectionHistoryRepository: DetectionHistoryRepository) :
    ViewModel() {

    fun getDetectionHistories(filter: String): LiveData<Resource<PagedList<DetectionHistoryEntity>>> =
        detectionHistoryRepository.getDetectionHistories(filter)

    fun getSearchQuery(query: String): LiveData<Resource<PagedList<DetectionHistoryEntity>>> =
        detectionHistoryRepository.getSearchDetectionHistories(query)

    fun deleteDetectionHistory(detectionHistory: DetectionHistoryEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            detectionHistoryRepository.deleteDetectionHistory(detectionHistory)
        }
    }

    fun addAllDetectionHistories(detectionHistories: List<DetectionHistoryEntity>) {
        viewModelScope.launch(Dispatchers.IO) {
            detectionHistoryRepository.addAllDetectionHistories(detectionHistories)
        }
    }

    fun addToDetectionHistory(detectionHistory: DetectionHistoryEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            detectionHistoryRepository.addToDetectionHistory(detectionHistory)
        }
    }

    fun deleteAll(detectionHistories: List<DetectionHistoryEntity>) =
        detectionHistoryRepository.deleteAllDetectionHistories(detectionHistories)
}