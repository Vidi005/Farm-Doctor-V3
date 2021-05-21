package com.android.farmdoctor.data.source.local.entity

import android.os.Parcelable
import androidx.annotation.NonNull
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Entity
@Parcelize
data class DetectionHistoryEntity(
    @PrimaryKey(autoGenerate = true)
    @NonNull
    @ColumnInfo
    var id: Int = 0,

    @ColumnInfo
    var image: ByteArray?,

    @ColumnInfo
    var name: String?,

    @ColumnInfo
    var accuracy: String?,

    @ColumnInfo
    var date: String?,

    @ColumnInfo
    var latency: String?,

    @ColumnInfo
    var latitude: String?,

    @ColumnInfo
    var longitude: String?,

    @ColumnInfo
    var detectionBased: String?
) : Parcelable {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as DetectionHistoryEntity

        if (id != other.id) return false
        if (image != null) {
            if (other.image == null) return false
            if (!image.contentEquals(other.image)) return false
        } else if (other.image != null) return false
        if (name != other.name) return false
        if (accuracy != other.accuracy) return false
        if (date != other.date) return false
        if (latency != other.latency) return false
        if (latitude != other.latitude) return false
        if (longitude != other.longitude) return false
        if (detectionBased != other.detectionBased) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id
        result = 31 * result + (image?.contentHashCode() ?: 0)
        result = 31 * result + (name?.hashCode() ?: 0)
        result = 31 * result + (accuracy?.hashCode() ?: 0)
        result = 31 * result + (date?.hashCode() ?: 0)
        result = 31 * result + (latency?.hashCode() ?: 0)
        result = 31 * result + (latitude?.hashCode() ?: 0)
        result = 31 * result + (longitude?.hashCode() ?: 0)
        result = 31 * result + (detectionBased?.hashCode() ?: 0)
        return result
    }
}
