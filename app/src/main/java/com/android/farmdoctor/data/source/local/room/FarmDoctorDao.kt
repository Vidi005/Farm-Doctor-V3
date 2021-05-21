package com.android.farmdoctor.data.source.local.room

import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData
import androidx.paging.DataSource
import androidx.room.*
import androidx.sqlite.db.SupportSQLiteQuery
import com.android.farmdoctor.data.source.local.entity.DetectionHistoryEntity
import com.android.farmdoctor.data.source.local.entity.PlantSpeciesEntity
import com.android.farmdoctor.data.source.local.entity.PlantSpeciesWithSynonym
import com.android.farmdoctor.data.source.local.entity.SynonymEntity

@Dao
interface FarmDoctorDao {

    @Transaction
    @Query("SELECT * FROM PlantSpeciesEntity WHERE scientificName = :sciName")
    fun getPlantSpeciesWithSynonymsBySciName(sciName: String): LiveData<PlantSpeciesWithSynonym>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertPlantSpecies(plantSpecies: PlantSpeciesEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertSynonyms(synonyms: List<SynonymEntity>)

    @RawQuery(observedEntities = [DetectionHistoryEntity::class])
    fun getDetectionHistories(query: SupportSQLiteQuery): DataSource.Factory<Int, DetectionHistoryEntity>

    @WorkerThread
    @Query("SELECT * FROM DetectionHistoryEntity WHERE name LIKE :searchQuery ORDER BY name ASC")
    fun getSearchDetectionHistories(searchQuery: String): DataSource.Factory<Int, DetectionHistoryEntity>

    @Delete
    fun deleteAllDetectionHistory(detectionHistories: List<DetectionHistoryEntity>)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertAllDetectionHistories(detectionHistories: List<DetectionHistoryEntity>)

    @Query("SELECT * FROM DetectionHistoryEntity WHERE id = :id")
    fun getDetailDetectionHistoryById(id: Int): LiveData<DetectionHistoryEntity>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertDetectionHistory(detectionHistory: DetectionHistoryEntity)

    @Delete
    suspend fun deleteDetectionHistory(detectionHistory: DetectionHistoryEntity)

    @Query("SELECT COUNT() FROM DetectionHistoryEntity WHERE accuracy = :acc")
    fun checkAccuracy(acc: String): Int
}