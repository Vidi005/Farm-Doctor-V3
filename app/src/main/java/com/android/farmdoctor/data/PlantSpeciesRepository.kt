package com.android.farmdoctor.data

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.android.farmdoctor.data.source.local.LocalDataSource
import com.android.farmdoctor.data.source.local.entity.MetaEntity
import com.android.farmdoctor.data.source.local.entity.PlantSpeciesEntity
import com.android.farmdoctor.data.source.local.entity.PlantSpeciesWithSynonym
import com.android.farmdoctor.data.source.local.entity.SynonymEntity
import com.android.farmdoctor.data.source.remote.ApiResponse
import com.android.farmdoctor.data.source.remote.RemoteDataSource
import com.android.farmdoctor.data.source.remote.response.DataItem
import com.android.farmdoctor.data.source.remote.response.Meta
import com.android.farmdoctor.vo.Resource

class PlantSpeciesRepository private constructor(
    private val remoteDataSource: RemoteDataSource,
    private val localDataSource: LocalDataSource) : PlantSpeciesDataSource {

    companion object {
        @Volatile
        private var INSTANCE: PlantSpeciesRepository? = null

        fun getInstance(remoteData: RemoteDataSource, localData: LocalDataSource):
            PlantSpeciesRepository = INSTANCE ?: synchronized(this) {
                PlantSpeciesRepository(remoteData, localData).apply { INSTANCE = this }
            }
    }

    override fun getPlantSpecies(searchQuery: String, page: Int, order: Map<String, String>):
        MutableLiveData<ApiResponse<List<PlantSpeciesEntity>>> {
        val plantSpeciesResults = MutableLiveData<ApiResponse<List<PlantSpeciesEntity>>>()
        remoteDataSource.getSearchQueryPlantSpecies(searchQuery, page, order,
            object : RemoteDataSource.LoadSearchPlantSpeciesCallback {
            override fun onSearchPlantSpeciesReceived(plantSpeciesResponse: ApiResponse<List<DataItem>>) {
                val plantSpeciesList = ArrayList<PlantSpeciesEntity>()
                plantSpeciesResponse.body.forEach {
                    val plantSpecies = with(it) {
                        PlantSpeciesEntity(id,
                            scientificName,
                            commonName,
                            imageUrl,
                            author,
                            rank,
                            "$year",
                            status,
                            genus,
                            family
                        )
                    }
                    plantSpeciesList.add(plantSpecies)
                }
                plantSpeciesResults.postValue(ApiResponse.success(plantSpeciesList))
            }
        })
        return plantSpeciesResults
    }

    override fun getMeta(searchQuery: String, page: Int, order: Map<String, String>):
        LiveData<ApiResponse<MetaEntity>>
    {
        val metaResult = MutableLiveData<ApiResponse<MetaEntity>>()
        remoteDataSource.getMeta(searchQuery, page, order, object :
            RemoteDataSource.LoadLinksCallback
        {
            override fun onGetMeta(metaResponse: ApiResponse<Meta>) {
                val metaData =  MetaEntity(metaResponse.body.total)
                metaResult.postValue(ApiResponse.success(metaData))
            }
        })
        return metaResult
    }

    override fun getPlantSpeciesWithSynonyms(scientificName: String):
        LiveData<Resource<PlantSpeciesWithSynonym>> =
        object : NetworkBoundResource<PlantSpeciesWithSynonym, DataItem>() {
            override suspend fun loadFromDB(): LiveData<PlantSpeciesWithSynonym> =
                localDataSource.getPlantSpeciesWithSynonyms(scientificName)

            override suspend fun shouldFetch(data: PlantSpeciesWithSynonym?): Boolean =
                data?.mPlantSpecies?.scientificName.isNullOrEmpty()

            override suspend fun createCall(): LiveData<ApiResponse<DataItem>> =
                remoteDataSource.getPlantSpeciesWithSynonyms(scientificName)

            override suspend fun saveCallResult(data: DataItem) {
                val detailPlantSpeciesEntity = with(data) {
                    PlantSpeciesEntity(id,
                        scientificName,
                        commonName,
                        imageUrl,
                        author,
                        rank,
                        year.toString(),
                        status,
                        genus,
                        family)
                }
                val synonymList = ArrayList<SynonymEntity>()
                if (data.synonyms.isNullOrEmpty()) SynonymEntity(0, 0, "")
                else data.synonyms.indices.forEach {
                    val synonym = with(data) { SynonymEntity(it + 1, id, synonyms[it]) }
                    synonymList.add(synonym)
                }
                localDataSource.apply {
                    insertPlantSpecies(detailPlantSpeciesEntity)
                    insertSynonyms(synonymList)
                }
            }
        }.asLiveData()
}