package com.android.farmdoctor.helper

import android.graphics.Bitmap

object CropToSquaredBitmapHelper {

    fun cropToSquaredBitmap(bitmap: Bitmap): Bitmap =
        if (bitmap.height <= bitmap.width) Bitmap.createBitmap(
            bitmap,
            bitmap.width/2 - bitmap.height/2,
            0,
            bitmap.height,
            bitmap.height
        ) else Bitmap.createBitmap(
            bitmap,
            0,
            bitmap.height/2 - bitmap.width/2,
            bitmap.width,
            bitmap.width
        )
}