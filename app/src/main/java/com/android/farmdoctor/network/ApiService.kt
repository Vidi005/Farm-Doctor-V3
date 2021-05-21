package com.android.farmdoctor.network

import com.android.farmdoctor.data.source.remote.response.TrefleResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query
import retrofit2.http.QueryMap

interface ApiService {

    @GET("/api/v1/species/search")
    fun getPlantSpecies(
        @Header("Authorization") authorization: String,
        @Query("q") searchQuery: String,
        @Query("page") page: Int,
        @QueryMap order: Map<String, String>
    ): Call<TrefleResponse>

    @GET("/api/v1/plants")
    fun getDetailPlantSpecies(
        @Header("Authorization") authorization: String,
        @Query("filter[scientific_name]") scientificName: String
    ): Call<TrefleResponse>
}