package com.android.farmdoctor.data.source.local.entity

import android.os.Parcelable
import androidx.annotation.NonNull
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Entity
@Parcelize
data class PlantSpeciesEntity(
    @PrimaryKey
    @NonNull
    @ColumnInfo
    val id: Int?,

    @ColumnInfo
    val scientificName: String?,

    @ColumnInfo
    val commonName: String?,

    @ColumnInfo
    val image: String?,

    @ColumnInfo
    val author: String?,

    @ColumnInfo
    val rank: String?,

    @ColumnInfo
    val year: String?,

    @ColumnInfo
    val status: String?,

    @ColumnInfo
    val genus: String?,

    @ColumnInfo
    val family: String?
) : Parcelable
