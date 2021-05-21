package com.android.farmdoctor.viewmodel

import android.graphics.Bitmap
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.farmdoctor.data.PlantDiseasesRepository
import com.android.farmdoctor.data.source.factory.model.Detection
import com.android.farmdoctor.data.source.local.entity.DetectionHistoryEntity
import com.android.farmdoctor.vo.Resource
import com.priyankvasa.android.cameraviewex.Image
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class DetectionViewModel(private val plantDiseasesRepository: PlantDiseasesRepository) :
    ViewModel() {

    private val _bitmap = MutableLiveData<Bitmap>()
    val bitmap: LiveData<Bitmap> get() = _bitmap

    fun setRecognizeTakenPicture(data: ByteArray) =
        plantDiseasesRepository.setRecognizeTakenPicture(data)

    fun getRecognizeTakenPicture(): LiveData<Resource<ArrayList<Detection>>> =
        plantDiseasesRepository.getRecognizeTakenPicture()

    fun setRecognizePreviewFrames(image: Image) =
        plantDiseasesRepository.setRecognizePreviewFrames(image)

    fun getRecognizePreviewFrames(): LiveData<Resource<ArrayList<Detection>>> =
        plantDiseasesRepository.getRecognizePreviewFrames()

    fun setRecognizeImage(bitmap: Bitmap) = plantDiseasesRepository.setRecognizeImage(bitmap)

    fun getRecognizeImage(): LiveData<Resource<ArrayList<Detection>>> =
        plantDiseasesRepository.getRecognizeImage()

    fun getBitmap(bitmap: Bitmap) {
        _bitmap.value = bitmap
    }

    fun addToDetectionHistory(detectionHistory: DetectionHistoryEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            plantDiseasesRepository.addToDetectionHistory(detectionHistory)
        }
    }

    fun checkAcc(acc: String): Boolean = plantDiseasesRepository.checkAccuracy(acc)
}