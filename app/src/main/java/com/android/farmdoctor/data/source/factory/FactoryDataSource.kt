package com.android.farmdoctor.data.source.factory

import android.graphics.Bitmap
import com.android.farmdoctor.data.source.factory.model.Detection
import com.android.farmdoctor.data.source.factory.model.PlantDisease
import com.android.farmdoctor.utils.Classification
import com.android.farmdoctor.utils.ListDiseases
import com.priyankvasa.android.cameraviewex.Image

class FactoryDataSource private constructor(
    private val listDiseases: ListDiseases,
    private val classification: Classification) {

    companion object {
        @Volatile
        private var INSTANCE: FactoryDataSource? = null

        fun getInstance(listDiseases: ListDiseases, classification: Classification):
            FactoryDataSource =
                INSTANCE ?: synchronized(this) {
                    FactoryDataSource(listDiseases, classification).apply { INSTANCE = this }
        }
    }

    fun getPlantDiseases(): ArrayList<PlantDisease> = listDiseases.addItem()

    fun getRecognizeTakenPicture(data: ByteArray): ArrayList<Detection> =
        classification.recognizeTakenPicture(data)

    fun getRecognizePreviewFrames(image: Image): ArrayList<Detection> =
        classification.recognizePreviewFrames(image)

    fun getRecognizeImage(bitmap: Bitmap): ArrayList<Detection> =
        classification.recognizeImage(bitmap)
}