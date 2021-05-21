package com.android.farmdoctor.data

import android.graphics.Bitmap
import androidx.lifecycle.LiveData
import com.android.farmdoctor.data.source.factory.model.Detection
import com.android.farmdoctor.data.source.factory.model.PlantDisease
import com.android.farmdoctor.data.source.local.entity.DetectionHistoryEntity
import com.android.farmdoctor.data.source.remote.service.LocationAddress
import com.android.farmdoctor.data.source.remote.service.LocationService
import com.android.farmdoctor.vo.Resource
import com.priyankvasa.android.cameraviewex.Image

interface PlantDiseasesDataSource {

    fun getPlantDiseases(): ArrayList<PlantDisease>
    fun setRecognizeTakenPicture(data: ByteArray)
    fun getRecognizeTakenPicture(): LiveData<Resource<ArrayList<Detection>>>
    fun setRecognizePreviewFrames(image: Image)
    fun getRecognizePreviewFrames(): LiveData<Resource<ArrayList<Detection>>>
    fun setRecognizeImage(bitmap: Bitmap)
    fun getRecognizeImage(): LiveData<Resource<ArrayList<Detection>>>
    fun setLastLocation(): LocationService
    fun getLastLocation()
    suspend fun addToDetectionHistory(detectionHistory: DetectionHistoryEntity)
    fun checkAccuracy(acc: String): Boolean
}