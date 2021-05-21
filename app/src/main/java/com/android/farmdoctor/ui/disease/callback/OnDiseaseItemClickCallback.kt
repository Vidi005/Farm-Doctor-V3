package com.android.farmdoctor.ui.disease.callback

import com.android.farmdoctor.data.source.factory.model.PlantDisease

interface OnDiseaseItemClickCallback {
    fun onItemClicked(data: PlantDisease)
}
