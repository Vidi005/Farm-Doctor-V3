package com.android.farmdoctor.data

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import com.android.farmdoctor.data.source.remote.ApiResponse
import com.android.farmdoctor.data.source.remote.StatusResponse
import com.android.farmdoctor.vo.Resource
import kotlinx.coroutines.*

abstract class NetworkBoundResource<ResultType, RequestType> {

    private val result = MediatorLiveData<Resource<ResultType>>()

    init {
        GlobalScope.launch(Dispatchers.Main) {
            result.value = Resource.loading(null)
            @Suppress("LeakingThis")
            val dbSource = loadFromDB()
            result.addSource(dbSource) {
                result.removeSource(dbSource)
                GlobalScope.launch(Dispatchers.Main) {
                    if (shouldFetch(it)) fetchFromNetwork(dbSource)
                    else result.addSource(dbSource) { newData ->
                        result.value = Resource.success(newData)
                    }
                }
            }
        }
    }

    protected fun onFetchFailed() {}

    protected abstract suspend fun loadFromDB(): LiveData<ResultType>

    protected abstract suspend fun shouldFetch(data: ResultType?): Boolean

    protected abstract suspend fun createCall(): LiveData<ApiResponse<RequestType>>

    protected abstract suspend fun saveCallResult(data: RequestType)

    private suspend fun fetchFromNetwork(dbSource: LiveData<ResultType>) {
        val apiResponse = createCall()
        result.addSource(dbSource) {
            result.value = Resource.loading(it)
        }
        result.addSource(apiResponse) { response ->
            result.apply {
                removeSource(apiResponse)
                removeSource(dbSource)
            }
            when (response.status) {
                StatusResponse.SUCCESS -> GlobalScope.launch(Dispatchers.IO) {
                    saveCallResult(response.body)
                    withContext(Dispatchers.Main) {
                        result.addSource(loadFromDB()) {
                            result.value = Resource.success(it)
                        }
                    }
                }
                StatusResponse.EMPTY -> CoroutineScope(Dispatchers.Main).launch {
                    result.addSource(loadFromDB()) {
                        result.value = Resource.success(it)
                    }
                }
                StatusResponse.ERROR -> {
                    onFetchFailed()
                    result.addSource(dbSource) {
                        result.value = Resource.error("${response.message}", it)
                    }
                }
            }
        }
    }

    fun asLiveData(): LiveData<Resource<ResultType>> = result
}