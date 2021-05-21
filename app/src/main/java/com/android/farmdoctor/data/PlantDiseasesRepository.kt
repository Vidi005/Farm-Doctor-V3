package com.android.farmdoctor.data

import android.graphics.Bitmap
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import com.android.farmdoctor.data.source.factory.FactoryDataSource
import com.android.farmdoctor.data.source.factory.model.Detection
import com.android.farmdoctor.data.source.factory.model.PlantDisease
import com.android.farmdoctor.data.source.local.LocalDataSource
import com.android.farmdoctor.data.source.local.entity.DetectionHistoryEntity
import com.android.farmdoctor.data.source.remote.RemoteDataSource
import com.android.farmdoctor.data.source.remote.service.LocationService
import com.android.farmdoctor.vo.Resource
import com.priyankvasa.android.cameraviewex.Image
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.Executor
import java.util.concurrent.Executors
import java.util.concurrent.Future

class PlantDiseasesRepository private constructor(
        private val remoteDataSource: RemoteDataSource,
        private val factoryDataSource: FactoryDataSource,
        private val localDataSource: LocalDataSource) : PlantDiseasesDataSource {

    companion object {
        @Volatile
        private var INSTANCE: PlantDiseasesRepository? = null

        fun getInstance(remoteData: RemoteDataSource,
            factoryData: FactoryDataSource,
            localData: LocalDataSource): PlantDiseasesRepository =
            INSTANCE ?: synchronized(this) {
                PlantDiseasesRepository(remoteData, factoryData, localData).apply { INSTANCE = this }
            }
        private val TAG = PlantDiseasesRepository::class.java.simpleName
    }
    private var detectionTask: Future<*>? = null
    private val takenPictureResults = MediatorLiveData<Resource<ArrayList<Detection>>>()
    private val previewFrameResults = MutableLiveData<Resource<ArrayList<Detection>>>()
    private val imageResults = MediatorLiveData<Resource<ArrayList<Detection>>>()
    private val mainThread = MainThreadExecutor()
    private var backgroundThread = Executors.newSingleThreadExecutor()

    private class MainThreadExecutor : Executor {
        private val mainThreadHandler = Handler(Looper.getMainLooper())
        override fun execute(command: Runnable) {
            mainThreadHandler.post(command)
        }
    }

    override fun getPlantDiseases(): ArrayList<PlantDisease> = factoryDataSource.getPlantDiseases()

    override fun setRecognizeTakenPicture(data: ByteArray) {
        takenPictureResults.value = Resource.loading(null)
        GlobalScope.launch(Dispatchers.IO) {
            try {
                val recognitions = factoryDataSource.getRecognizeTakenPicture(data)
                Resource.loading(recognitions)
                Log.d(TAG, "$recognitions")
                withContext(Dispatchers.Main) {
                    takenPictureResults.postValue(Resource.success(recognitions))
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Resource.error("${e.message}", factoryDataSource.getRecognizeTakenPicture(data))
                    Log.d(TAG, "${e.message}")
                }
            }
        }
    }

    override fun getRecognizeTakenPicture(): LiveData<Resource<ArrayList<Detection>>> =
        takenPictureResults

    override fun setRecognizePreviewFrames(image: Image) {
        if (detectionTask?.isDone == false && detectionTask != null) {
            detectionTask?.cancel(true)
            detectionTask = null
        }
        detectionTask = backgroundThread.submit {
            try {
                val recognitions = factoryDataSource.getRecognizePreviewFrames(image)
                previewFrameResults.postValue(Resource.success(recognitions))
            } catch (e: Exception) {
                mainThread.execute {
                    Resource.error(e.message.toString(),
                        factoryDataSource.getRecognizePreviewFrames(image))
                }
            }
        }
    }

    override fun getRecognizePreviewFrames(): LiveData<Resource<ArrayList<Detection>>> =
        previewFrameResults

    override fun setRecognizeImage(bitmap: Bitmap) {
        imageResults.value = Resource.loading(null)
        GlobalScope.launch(Dispatchers.IO) {
            try {
                val recognitions = factoryDataSource.getRecognizeImage(bitmap)
                Resource.loading(recognitions)
                withContext(Dispatchers.Main) {
                    imageResults.postValue(Resource.success(recognitions))
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Resource.error("${e.message}", factoryDataSource.getRecognizeImage(bitmap))
                    Log.d(TAG, "${e.message}")
                }
            }
        }
    }

    override fun getRecognizeImage(): LiveData<Resource<ArrayList<Detection>>> = imageResults

    override fun setLastLocation(): LocationService = remoteDataSource.setLastLocation()

    override fun getLastLocation() = remoteDataSource.getLastLocation()

    override suspend fun addToDetectionHistory(detectionHistory: DetectionHistoryEntity) =
        localDataSource.insertDetectionHistory(detectionHistory)

    override fun checkAccuracy(acc: String): Boolean {
        var isAccSame = false
        GlobalScope.launch(Dispatchers.IO) { isAccSame = localDataSource.checkAccuracy(acc) > 0 }
        return isAccSame
    }
}