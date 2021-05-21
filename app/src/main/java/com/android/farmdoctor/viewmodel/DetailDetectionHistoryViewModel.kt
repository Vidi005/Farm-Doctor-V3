package com.android.farmdoctor.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.farmdoctor.data.DetectionHistoryRepository
import com.android.farmdoctor.data.source.local.entity.DetectionHistoryEntity
import com.android.farmdoctor.vo.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class DetailDetectionHistoryViewModel(
    private val detectionHistoryRepository: DetectionHistoryRepository) : ViewModel() {

    fun getDetailDetectionHistoryById(id: Int): LiveData<Resource<DetectionHistoryEntity>> =
        detectionHistoryRepository.getDetailDetectionHistoryById(id)

    fun deleteDetectionHistory(detectionHistory: DetectionHistoryEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            detectionHistoryRepository.deleteDetectionHistory(detectionHistory)
        }
    }
}