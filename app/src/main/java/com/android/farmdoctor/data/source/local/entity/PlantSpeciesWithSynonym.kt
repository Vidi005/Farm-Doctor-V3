package com.android.farmdoctor.data.source.local.entity

import androidx.room.Embedded
import androidx.room.Relation

data class PlantSpeciesWithSynonym(
    @Embedded
    val mPlantSpecies: PlantSpeciesEntity,

    @Relation(parentColumn = "id", entityColumn = "id")
    val mSynonym: List<SynonymEntity>
)
