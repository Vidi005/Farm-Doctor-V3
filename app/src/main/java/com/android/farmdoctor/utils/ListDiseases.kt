package com.android.farmdoctor.utils

import android.app.Activity
import android.content.res.TypedArray
import com.android.farmdoctor.R
import com.android.farmdoctor.data.source.factory.model.PlantDisease

class ListDiseases(private val activity: Activity) {

    private lateinit var dataPicture: TypedArray
    private lateinit var dataName: Array<String>
    private lateinit var dataDetail: TypedArray

    fun addItem(): ArrayList<PlantDisease> {
        prepare()
        val plantDiseases = ArrayList<PlantDisease>()
        dataName.indices.forEach {
            val plantDisease = PlantDisease(
                dataPicture.getResourceId(it, -1),
                dataName[it],
                dataDetail.getResourceId(it, -1)
            )
            plantDiseases.add(plantDisease)
        }
        dataPicture.recycle()
        return plantDiseases
    }

    private fun prepare() {
        dataPicture = activity.resources.obtainTypedArray(R.array.leaves_picture)
        dataName = activity.resources.getStringArray(R.array.name)
        dataDetail = activity.resources.obtainTypedArray(R.array.detail_layout)
    }
}