package com.android.farmdoctor.ui.plant.callback

import com.android.farmdoctor.data.source.local.entity.PlantSpeciesEntity

interface OnPlantItemClickCallback {
    fun onItemClicked(data: PlantSpeciesEntity)
}
