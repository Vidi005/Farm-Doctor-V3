package com.android.farmdoctor.data.source.remote

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.android.farmdoctor.BuildConfig
import com.android.farmdoctor.data.source.remote.response.DataItem
import com.android.farmdoctor.data.source.remote.response.Meta
import com.android.farmdoctor.data.source.remote.response.TrefleResponse
import com.android.farmdoctor.data.source.remote.service.LocationAddress
import com.android.farmdoctor.data.source.remote.service.LocationService
import com.android.farmdoctor.network.ApiConfig.getApiService
import com.android.farmdoctor.utils.Geocode
import com.android.farmdoctor.utils.LocationHelper
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RemoteDataSource private constructor(
    private val locationHelper: LocationHelper,
    private val geocode: Geocode){

    companion object {
        @Volatile
        private var INSTANCE: RemoteDataSource? = null

        fun getInstance(locationHelper: LocationHelper, geocode: Geocode): RemoteDataSource =
            INSTANCE ?: synchronized(this) {
                RemoteDataSource(locationHelper, geocode).apply { INSTANCE = this }
        }

        private const val API_KEY = "token ${BuildConfig.TrefleAPIKEY}"
        private val TAG = RemoteDataSource::class.java.simpleName
    }

    fun getSearchQueryPlantSpecies(
        searchQuery: String,
        page: Int,
        order: Map<String, String>,
        callback: LoadSearchPlantSpeciesCallback)
    {
        val client = getApiService().getPlantSpecies(API_KEY, searchQuery, page, order)
        client.enqueue(object : Callback<TrefleResponse> {
            override fun onResponse(
                call: Call<TrefleResponse>,
                response: Response<TrefleResponse>
            ) {
                if (response.isSuccessful) callback.onSearchPlantSpeciesReceived(
                    ApiResponse.success(response.body()?.data as List<DataItem>))
                else {
                    ApiResponse.empty(response.message(), response.errorBody())
                    Log.e(TAG, response.message())
                }
            }

            override fun onFailure(call: Call<TrefleResponse>, t: Throwable) {
                ApiResponse.error("${t.message}", null)
                Log.e(TAG, "${t.message}")
            }
        })
    }

    interface LoadSearchPlantSpeciesCallback {
        fun onSearchPlantSpeciesReceived(plantSpeciesResponse: ApiResponse<List<DataItem>>)
    }

    fun getMeta(
        searchQuery: String,
        page: Int,
        order: Map<String, String>,
        callback: LoadLinksCallback)
    {
        val client = getApiService().getPlantSpecies(API_KEY, searchQuery, page, order)
        client.enqueue(object : Callback<TrefleResponse> {
            override fun onResponse(call: Call<TrefleResponse>, response: Response<TrefleResponse>) {
                if (response.isSuccessful)
                    callback.onGetMeta(ApiResponse.success(response.body()?.meta as Meta))
                else {
                    ApiResponse.empty(response.message(), response.errorBody())
                    Log.e(TAG, response.message())
                }
            }

            override fun onFailure(call: Call<TrefleResponse>, t: Throwable) {
                ApiResponse.error("${t.message}", null)
                Log.e(TAG, "${t.message}")
            }
        })
    }

    interface LoadLinksCallback {
        fun onGetMeta(metaResponse: ApiResponse<Meta>)
    }

    fun getPlantSpeciesWithSynonyms(sciName: String): MutableLiveData<ApiResponse<DataItem>> {
        val resultDetailPlantSpecies = MutableLiveData<ApiResponse<DataItem>>()
        val client = getApiService().getDetailPlantSpecies(API_KEY, sciName)
        client.enqueue(object : Callback<TrefleResponse> {
            override fun onResponse(call: Call<TrefleResponse>, response: Response<TrefleResponse>) {
                if (response.isSuccessful)
                    resultDetailPlantSpecies
                        .postValue(ApiResponse.success(response.body()?.data?.get(0) as DataItem))
                else {
                    ApiResponse.empty(response.message(), response.errorBody())
                    Log.e(TAG, response.message())
                }
            }

            override fun onFailure(call: Call<TrefleResponse>, t: Throwable) {
                ApiResponse.error("${t.message}", null)
                Log.e(TAG, "${t.message}")
            }
        })
        return resultDetailPlantSpecies
    }

    fun setLastLocation(): LocationService =
        LocationService(locationHelper.dataLatitude, locationHelper.dataLongitude)

    fun getLastLocation() = locationHelper.getLastLocation()

    fun geocodeLocationAddress(latitude: Double, longitude: Double): LocationAddress =
        geocode.setLocationAddress(latitude, longitude)
}