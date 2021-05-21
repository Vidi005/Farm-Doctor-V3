package com.android.farmdoctor.utils

import android.app.Activity
import android.content.res.TypedArray
import android.graphics.*
import android.util.Log
import com.android.farmdoctor.R
import com.android.farmdoctor.data.source.factory.model.Detection
import com.priyankvasa.android.cameraviewex.Image
import org.tensorflow.lite.Interpreter
import java.io.ByteArrayOutputStream
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel

class Classification(private val activity: Activity) {

    companion object {
        private const val BATCH_SIZE = 1
        private const val MODEL_INPUT_SIZE = 224
        private const val BYTES_PER_CHANNEL = 4
        private const val CHANNEL_SIZE = 3
        private const val IMAGE_STD = 255f
    }

    private lateinit var dataPicture: TypedArray
    private lateinit var dataDetail: TypedArray
    private val tfLiteModel = Interpreter(getModelByteBuffer(), Interpreter.Options())
    private val dataName = activity.resources.getStringArray(R.array.name)

    private fun getModelByteBuffer(): MappedByteBuffer {
        val modelPath = "Plant_Leaf_Diseases_Classification_MobileNet.tflite"
        val fileDescriptor = activity.assets.openFd(modelPath)
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        val startOffset = fileDescriptor.startOffset
        val declaredLength = fileDescriptor.declaredLength
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }

    private fun prepare() {
        dataPicture = activity.resources.obtainTypedArray(R.array.leaves_picture)
        dataDetail = activity.resources.obtainTypedArray(R.array.detail_layout)
    }

    private fun parseResults(result: Array<FloatArray>): ArrayList<Detection> {
        prepare()
        val recognitions = ArrayList<Detection>()
        val dataAcc = result[0]
        dataName.indices.forEach {
            val detection = Detection(
                dataName[it],
                dataAcc[it] * 100,
                dataPicture.getResourceId(it, -1),
                dataDetail.getResourceId(it, -1)
            )
            recognitions.add(detection)
        }
        dataPicture.recycle()
        dataDetail.recycle()
        return recognitions
    }

    fun recognizeTakenPicture(data: ByteArray): ArrayList<Detection> {
        val unscaledBitmap = BitmapFactory.decodeByteArray(data, 0, data.size)
        val bitmap = Bitmap.createScaledBitmap(
            unscaledBitmap,
            MODEL_INPUT_SIZE,
            MODEL_INPUT_SIZE,
            false)
        val byteBuffer = convertBitmapToByteBuffer(bitmap)
        Log.d("Classification", "ByteBuffer: $byteBuffer")
        val result = Array(BATCH_SIZE) { FloatArray(dataName.size) }
        tfLiteModel.run(byteBuffer, result)
        return parseResults(result)
    }

    fun recognizePreviewFrames(image: Image): ArrayList<Detection> {
        val data: ByteArray
        val yuvImage = YuvImage(
            image.data,
            ImageFormat.NV21,
            image.width,
            image.height,
            null
        )
        val jpegDataStream = ByteArrayOutputStream()
        val previewFrameScale = 0.4f
        yuvImage.compressToJpeg(
            Rect(0, 0, image.width, image.height),
            (100 * previewFrameScale).toInt(),
            jpegDataStream
        )
        data = jpegDataStream.toByteArray()
        val options = BitmapFactory.Options()
        options.inPreferredConfig = Bitmap.Config.ARGB_8888
        val unscaledBitmap = BitmapFactory.decodeByteArray(data, 0, data.size)
        val bitmap = Bitmap.createScaledBitmap(
            unscaledBitmap,
            MODEL_INPUT_SIZE,
            MODEL_INPUT_SIZE,
            false)
        val byteBuffer = convertBitmapToByteBuffer(bitmap)
        val result = Array(BATCH_SIZE) { FloatArray(dataName.size) }
        tfLiteModel.run(byteBuffer, result)
        return parseResults(result)
    }

    fun recognizeImage(bitmap: Bitmap?): ArrayList<Detection> {
        val bitmapData = bitmap?.let { Bitmap.createScaledBitmap(
            it,
            MODEL_INPUT_SIZE,
            MODEL_INPUT_SIZE,
            false)
        }
        val byteBuffer = convertBitmapToByteBuffer(bitmapData)
        val result = Array(BATCH_SIZE) { FloatArray(dataName.size) }
        tfLiteModel.run(byteBuffer, result)
        return parseResults(result)
    }

    private fun convertBitmapToByteBuffer(bitmap: Bitmap?): ByteBuffer {
        val byteBuffer = ByteBuffer.allocateDirect(
            BATCH_SIZE *
            MODEL_INPUT_SIZE *
            MODEL_INPUT_SIZE *
            BYTES_PER_CHANNEL *
            CHANNEL_SIZE
        ).order(ByteOrder.nativeOrder())
        val pixelValues = IntArray(MODEL_INPUT_SIZE * MODEL_INPUT_SIZE)
        bitmap?.getPixels(pixelValues, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)
        var pixel = 0
        repeat((0 until MODEL_INPUT_SIZE).count()) {
            repeat((0 until MODEL_INPUT_SIZE).count()) {
                val pixelValue = pixelValues[pixel++]
                byteBuffer.putFloat((pixelValue shr 16 and 0xFF) / IMAGE_STD)
                byteBuffer.putFloat((pixelValue shr 8 and 0xFF) / IMAGE_STD)
                byteBuffer.putFloat((pixelValue and 0xFF) / IMAGE_STD)
            }
        }
        return byteBuffer
    }
}