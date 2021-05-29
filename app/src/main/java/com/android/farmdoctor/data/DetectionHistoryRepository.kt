package com.android.farmdoctor.data

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import com.android.farmdoctor.data.source.local.LocalDataSource
import com.android.farmdoctor.data.source.local.entity.DetectionHistoryEntity
import com.android.farmdoctor.data.source.remote.RemoteDataSource
import com.android.farmdoctor.data.source.remote.service.LocationAddress
import com.android.farmdoctor.vo.Resource
import kotlinx.coroutines.*

class DetectionHistoryRepository private constructor(
    private val localDataSource: LocalDataSource,
    private val remoteDataSource: RemoteDataSource) :
    DetectionHistoryDataSource {

    companion object {
        @Volatile
        private var INSTANCE: DetectionHistoryRepository? = null

        fun getInstance(localData: LocalDataSource, remoteData: RemoteDataSource):
            DetectionHistoryRepository = INSTANCE ?: synchronized(this) {
                DetectionHistoryRepository(localData, remoteData).apply { INSTANCE = this }
            }
    }
    private val geocodeResult = MediatorLiveData<Resource<LocationAddress>>()

    override fun getDetectionHistories(filter: String):
        LiveData<Resource<PagedList<DetectionHistoryEntity>>>
    {
        val config = PagedList.Config.Builder()
            .setEnablePlaceholders(true)
            .setInitialLoadSizeHint(10)
            .setPageSize(10)
            .build()
        val result = MediatorLiveData<Resource<PagedList<DetectionHistoryEntity>>>()
        result.value = Resource.loading(null)
        GlobalScope.launch(Dispatchers.Main) {
            try {
                result.addSource(LivePagedListBuilder(localDataSource.getDetectionHistories(filter),
                    config).build()) {
                        result.value = Resource.loading(it)
                }
                val deferredDetectionHistories = async(Dispatchers.IO) {
                    localDataSource.getDetectionHistories(filter)
                }
                val detectionHistories =
                    LivePagedListBuilder(deferredDetectionHistories.await(), config).build()
                result.addSource(detectionHistories) {
                    result.removeSource(detectionHistories)
                    result.postValue(Resource.success(it))
                }
            } catch (e: Exception) {
                Resource.error("${e.message}", localDataSource.getDetectionHistories(filter))
            }
        }
        return result
    }

    override fun getSearchDetectionHistories(searchQuery: String):
        LiveData<Resource<PagedList<DetectionHistoryEntity>>> {
        val config = PagedList.Config.Builder()
            .setEnablePlaceholders(true)
            .setInitialLoadSizeHint(5)
            .setPageSize(5)
            .build()
        val result = MediatorLiveData<Resource<PagedList<DetectionHistoryEntity>>>()
        result.value = Resource.loading(null)
        GlobalScope.launch(Dispatchers.Main) {
            try {
                result.addSource(LivePagedListBuilder(
                    localDataSource.getSearchDetectionHistories(searchQuery), config).build()) {
                        result.value = Resource.loading(it)
                }
                val deferredSearchQuery = async(Dispatchers.IO) {
                    localDataSource.getSearchDetectionHistories(searchQuery)
                }
                val searchQueryResult =
                    LivePagedListBuilder(deferredSearchQuery.await(), config).build()
                result.addSource(searchQueryResult) {
                    result.removeSource(searchQueryResult)
                    result.postValue(Resource.success(it))
                }
            } catch (e: Exception) {
                Resource.error("${e.message}", localDataSource.getSearchDetectionHistories(searchQuery))
            }
        }
        return result
    }

    override fun deleteAllDetectionHistories(detectionHistories: List<DetectionHistoryEntity>) =
        localDataSource.deleteAllDetectionHistories(detectionHistories)

    override fun getDetailDetectionHistoryById(id: Int): LiveData<Resource<DetectionHistoryEntity>> {
        val result = MediatorLiveData<Resource<DetectionHistoryEntity>>()
        result.value = Resource.loading(null)
        GlobalScope.launch(Dispatchers.Main) {
            try {
                result.addSource(localDataSource.getDetailDetectionHistoryById(id)) {
                    result.value = Resource.loading(it)
                }
                val deferredDetailDetectionHistory = async(Dispatchers.IO) {
                    localDataSource.getDetailDetectionHistoryById(id)
                }
                val detailDetectionHistory = deferredDetailDetectionHistory.await()
                result.addSource(detailDetectionHistory) {
                    result.removeSource(detailDetectionHistory)
                    result.postValue(Resource.success(it))
                }
            } catch (e: Exception) {
                Resource.error("${e.message}", localDataSource.getDetailDetectionHistoryById(id))
            }
        }
        return result
    }

    override suspend fun deleteDetectionHistory(detectionHistory: DetectionHistoryEntity) =
        localDataSource.deleteDetectionHistory(detectionHistory)

    override suspend fun addAllDetectionHistories(detectionHistories: List<DetectionHistoryEntity>) =
        localDataSource.insertAllDetectionHistories(detectionHistories)

    override suspend fun addToDetectionHistory(detectionHistory: DetectionHistoryEntity) =
        localDataSource.insertDetectionHistory(detectionHistory)

    override fun geocodeLocationAddress(latitude: Double, longitude: Double) {
        geocodeResult.value = Resource.loading(null)
        GlobalScope.launch(Dispatchers.IO) {
            try {
                for (i in 0..2) {
                    val geocodeLocation = remoteDataSource.geocodeLocationAddress(latitude, longitude)
                    delay(1000L)
                    Resource.loading(geocodeLocation)
                    withContext(Dispatchers.Main) {
                        geocodeResult.postValue(Resource.success(geocodeLocation))
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Resource.error("${e.message}",
                        remoteDataSource.geocodeLocationAddress(latitude, longitude))
                }
            }
        }
    }

    override fun getLocationAddress(): LiveData<Resource<LocationAddress>> = geocodeResult
}