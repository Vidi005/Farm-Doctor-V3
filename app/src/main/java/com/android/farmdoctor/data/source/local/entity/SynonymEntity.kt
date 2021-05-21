package com.android.farmdoctor.data.source.local.entity

import android.os.Parcelable
import androidx.annotation.NonNull
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(primaryKeys = ["number", "id"],
    foreignKeys = [ForeignKey(entity = PlantSpeciesEntity::class,
        parentColumns = ["id"],
        childColumns = ["id"])],
    indices = [Index(value = ["number"]),
        Index(value = ["id"])])
data class SynonymEntity(
    @NonNull
    @ColumnInfo
    val number: Int,

    @NonNull
    @ColumnInfo
    val id: Int,

    @ColumnInfo
    val synonym: String?
) : Parcelable
