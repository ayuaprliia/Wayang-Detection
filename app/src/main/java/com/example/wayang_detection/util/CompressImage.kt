package com.example.wayang_detection.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.ByteArrayOutputStream
import androidx.core.graphics.scale

fun compressImage(
    context: Context,
    resId: Int,
    maxWidth: Int = 512,
    maxHeight: Int = 512,
    quality: Int = 40
): Bitmap {
    // Decode bounds dulu (biar hemat memory)
    val options = BitmapFactory.Options().apply {
        inJustDecodeBounds = true
    }
    BitmapFactory.decodeResource(context.resources, resId, options)

    // Hitung sample size
    options.inSampleSize = calculateInSampleSize(options, maxWidth, maxHeight)
    options.inJustDecodeBounds = false

    val bitmap = BitmapFactory.decodeResource(context.resources, resId, options)

    // Resize lagi biar presisi
    val scaledBitmap = bitmap.scale(maxWidth, maxHeight)

    // Compress ke byte array
    val stream = ByteArrayOutputStream()
    scaledBitmap.compress(Bitmap.CompressFormat.JPEG, quality, stream)

    return scaledBitmap
}

fun calculateInSampleSize(
    options: BitmapFactory.Options,
    reqWidth: Int,
    reqHeight: Int
): Int {
    val (height: Int, width: Int) = options.outHeight to options.outWidth
    var inSampleSize = 1

    if (height > reqHeight || width > reqWidth) {
        val halfHeight = height / 2
        val halfWidth = width / 2

        while (halfHeight / inSampleSize >= reqHeight &&
            halfWidth / inSampleSize >= reqWidth
        ) {
            inSampleSize *= 2
        }
    }
    return inSampleSize
}