package com.android.farmdoctor.di

import android.app.Activity
import com.android.farmdoctor.data.DetectionHistoryRepository
import com.android.farmdoctor.data.PlantDiseasesRepository
import com.android.farmdoctor.data.PlantSpeciesRepository
import com.android.farmdoctor.data.source.factory.FactoryDataSource
import com.android.farmdoctor.data.source.local.LocalDataSource
import com.android.farmdoctor.data.source.local.room.FarmDoctorDatabase
import com.android.farmdoctor.data.source.remote.RemoteDataSource
import com.android.farmdoctor.utils.Classification
import com.android.farmdoctor.utils.Geocode
import com.android.farmdoctor.utils.ListDiseases
import com.android.farmdoctor.utils.LocationHelper

object Injection {

    fun providePlantSpeciesRepository(activity: Activity): PlantSpeciesRepository {
        val database = FarmDoctorDatabase.getInstance(activity)
        val remoteDataSource = RemoteDataSource.getInstance(LocationHelper(activity), Geocode(activity))
        val localDataSource = LocalDataSource.getInstance(database.farmDoctorDao())
        return PlantSpeciesRepository.getInstance(remoteDataSource, localDataSource)
    }

    fun providePlantDiseasesRepository(activity: Activity): PlantDiseasesRepository {
        val locationManager = LocationHelper(activity)
        val geocode = Geocode(activity)
        val database = FarmDoctorDatabase.getInstance(activity)
        val plantDiseases = ListDiseases(activity)
        val classifier = Classification(activity)
        val remoteDataSource = RemoteDataSource.getInstance(locationManager, geocode)
        val localDataSource = LocalDataSource.getInstance(database.farmDoctorDao())
        val factoryDataSource = FactoryDataSource.getInstance(plantDiseases, classifier)
        return PlantDiseasesRepository.getInstance(remoteDataSource, factoryDataSource, localDataSource)
    }

    fun provideDetectionHistoryRepository(activity: Activity): DetectionHistoryRepository {
        val locationManager = LocationHelper(activity)
        val geocode = Geocode(activity)
        val database = FarmDoctorDatabase.getInstance(activity)
        val remoteDataSource = RemoteDataSource.getInstance(locationManager, geocode)
        val localDataSource = LocalDataSource.getInstance(database.farmDoctorDao())
        return DetectionHistoryRepository.getInstance(localDataSource, remoteDataSource)
    }
}