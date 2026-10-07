package com.example.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import android.util.Log
import java.io.ByteArrayOutputStream

object ImageUtils {
    private const val TAG = "ImageUtils"

    /**
     * Converts an image Uri from PhotoPicker to a compressed Base64 JPEG string.
     * Scales image down to fit maxWidth and maxHeight to optimize storage and transmission.
     */
    fun uriToBase64(
        context: Context,
        uri: Uri,
        maxWidth: Int = 800,
        maxHeight: Int = 800,
        quality: Int = 75
    ): String? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return null
            val originalBitmap = BitmapFactory.decodeStream(inputStream)
            inputStream.close()
            if (originalBitmap == null) return null

            // Calculate proportional scale
            val widthRatio = maxWidth.toFloat() / originalBitmap.width
            val heightRatio = maxHeight.toFloat() / originalBitmap.height
            val scale = minOf(widthRatio, heightRatio, 1.0f)

            val scaledBitmap = if (scale < 1.0f) {
                val targetW = (originalBitmap.width * scale).toInt().coerceAtLeast(1)
                val targetH = (originalBitmap.height * scale).toInt().coerceAtLeast(1)
                Bitmap.createScaledBitmap(originalBitmap, targetW, targetH, true)
            } else {
                originalBitmap
            }

            val outputStream = ByteArrayOutputStream()
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
            val bytes = outputStream.toByteArray()
            Base64.encodeToString(bytes, Base64.NO_WRAP)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to encode uri to base64: ${e.message}", e)
            null
        }
    }

    /**
     * Decodes a Base64 string back to an Android Bitmap for UI rendering.
     */
    fun base64ToBitmap(base64: String?): Bitmap? {
        if (base64.isNullOrBlank()) return null
        return try {
            val cleanBase64 = if (base64.contains(",")) {
                base64.substringAfter(",")
            } else {
                base64
            }
            val decodedBytes = Base64.decode(cleanBase64, Base64.DEFAULT)
            BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to decode base64 to bitmap: ${e.message}")
            null
        }
    }
}
