package com.android.farmdoctor.data.source.local.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.android.farmdoctor.data.source.local.entity.DetectionHistoryEntity
import com.android.farmdoctor.data.source.local.entity.PlantSpeciesEntity
import com.android.farmdoctor.data.source.local.entity.SynonymEntity

@Database(entities = [PlantSpeciesEntity::class,
        SynonymEntity::class,
        DetectionHistoryEntity::class],
    version = 1,
    exportSchema = false)
abstract class FarmDoctorDatabase : RoomDatabase() {

    abstract fun farmDoctorDao(): FarmDoctorDao
    companion object {
        @Volatile
        private var INSTANCE: FarmDoctorDatabase? = null

        fun getInstance(context: Context): FarmDoctorDatabase = INSTANCE ?: synchronized(this) {
            Room.databaseBuilder(context.applicationContext,
                FarmDoctorDatabase::class.java,
                "farm_doctor.db").build().apply { INSTANCE = this }
        }
    }
}