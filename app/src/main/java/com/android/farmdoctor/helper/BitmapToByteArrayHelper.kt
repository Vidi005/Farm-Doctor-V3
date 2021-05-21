package com.android.farmdoctor.helper

import android.graphics.Bitmap
import java.io.ByteArrayOutputStream

object BitmapToByteArrayHelper {

    fun bitmapToByteArrayConverter(bitmap: Bitmap): ByteArray {
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, stream)
        return stream.toByteArray()
    }
}