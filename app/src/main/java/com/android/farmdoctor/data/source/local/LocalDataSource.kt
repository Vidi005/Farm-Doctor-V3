package com.android.farmdoctor.data.source.local

import androidx.lifecycle.LiveData
import androidx.paging.DataSource
import com.android.farmdoctor.data.source.local.entity.DetectionHistoryEntity
import com.android.farmdoctor.data.source.local.entity.PlantSpeciesEntity
import com.android.farmdoctor.data.source.local.entity.PlantSpeciesWithSynonym
import com.android.farmdoctor.data.source.local.entity.SynonymEntity
import com.android.farmdoctor.data.source.local.room.FarmDoctorDao
import com.android.farmdoctor.helper.FilterUtils.getDetectionHistoriesFilteredQuery

class LocalDataSource private constructor(private val mFarmDoctorDao: FarmDoctorDao) {

    companion object {
        private val INSTANCE: LocalDataSource? = null

        fun getInstance(farmDoctorDao: FarmDoctorDao): LocalDataSource =
            INSTANCE ?: LocalDataSource(farmDoctorDao)
    }

    fun getPlantSpeciesWithSynonyms(sciName: String): LiveData<PlantSpeciesWithSynonym> =
        mFarmDoctorDao.getPlantSpeciesWithSynonymsBySciName(sciName)

    fun insertPlantSpecies(plantSpecies: PlantSpeciesEntity) =
        mFarmDoctorDao.insertPlantSpecies(plantSpecies)

    fun insertSynonyms(synonyms: List<SynonymEntity>) = mFarmDoctorDao.insertSynonyms(synonyms)

    fun getDetectionHistories(query: String): DataSource.Factory<Int, DetectionHistoryEntity> {
        val filter = getDetectionHistoriesFilteredQuery(query)
        return mFarmDoctorDao.getDetectionHistories(filter)
    }

    fun getSearchDetectionHistories(searchQuery: String):
        DataSource.Factory<Int, DetectionHistoryEntity> =
            mFarmDoctorDao.getSearchDetectionHistories(searchQuery)

    suspend fun insertDetectionHistory(detectionHistory: DetectionHistoryEntity) =
        mFarmDoctorDao.insertDetectionHistory(detectionHistory)

    suspend fun insertAllDetectionHistories(detectionHistories: List<DetectionHistoryEntity>) =
        mFarmDoctorDao.insertAllDetectionHistories(detectionHistories)

    fun getDetailDetectionHistoryById(id: Int): LiveData<DetectionHistoryEntity> =
        mFarmDoctorDao.getDetailDetectionHistoryById(id)

    fun checkAccuracy(acc: String): Int = mFarmDoctorDao.checkAccuracy(acc)
    
    fun deleteAllDetectionHistories(detectionHistories: List<DetectionHistoryEntity>) =
        mFarmDoctorDao.deleteAllDetectionHistory(detectionHistories)

    suspend fun deleteDetectionHistory(detectionHistory: DetectionHistoryEntity) {
        mFarmDoctorDao.deleteDetectionHistory(detectionHistory)
    }
}