package com.example.qr

import android.graphics.Bitmap
import android.graphics.Color
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import java.util.EnumMap

object QrCodeGenerator {

    /**
     * Generates a QR Code Bitmap with specified content and dimensions.
     */
    fun generateQrBitmap(
        content: String,
        width: Int = 512,
        height: Int = 512,
        darkColor: Int = Color.parseColor("#0F1E36"), // Library Navy
        lightColor: Int = Color.WHITE
    ): Bitmap? {
        return try {
            val hints = EnumMap<EncodeHintType, Any>(EncodeHintType::class.java).apply {
                put(EncodeHintType.CHARACTER_SET, "UTF-8")
                put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H)
                put(EncodeHintType.MARGIN, 2)
            }

            val writer = QRCodeWriter()
            val bitMatrix = writer.encode(content, BarcodeFormat.QR_CODE, width, height, hints)

            val matrixWidth = bitMatrix.width
            val matrixHeight = bitMatrix.height
            val pixels = IntArray(matrixWidth * matrixHeight)

            for (y in 0 until matrixHeight) {
                val offset = y * matrixWidth
                for (x in 0 until matrixWidth) {
                    pixels[offset + x] = if (bitMatrix.get(x, y)) darkColor else lightColor
                }
            }

            val bitmap = Bitmap.createBitmap(matrixWidth, matrixHeight, Bitmap.Config.ARGB_8888)
            bitmap.setPixels(pixels, 0, matrixWidth, 0, 0, matrixWidth, matrixHeight)
            bitmap
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Formats official library reception QR code payload
     */
    fun formatOfficialReceptionQr(token: String, libraryName: String): String {
        return "RSLIBRARY:OFFICIAL:$token:$libraryName"
    }

    /**
     * Formats individual student QR payload
     */
    fun formatStudentQr(studentId: String, studentName: String): String {
        return "RSLIBRARY:STUDENT:$studentId:$studentName"
    }

    /**
     * Parse scanned QR code payload
     */
    fun parseQrCode(rawText: String): ParsedQrResult {
        val trimmed = rawText.trim()
        if (trimmed.startsWith("RSLIBRARY:OFFICIAL:")) {
            val parts = trimmed.split(":")
            val token = parts.getOrNull(2) ?: ""
            return ParsedQrResult.OfficialReception(token)
        }
        if (trimmed.startsWith("RSLIBRARY:STUDENT:")) {
            val parts = trimmed.split(":")
            val studentId = parts.getOrNull(2) ?: ""
            val name = parts.getOrNull(3) ?: ""
            return ParsedQrResult.StudentBadge(studentId, name)
        }
        // Direct token or student ID fallback
        if (trimmed.startsWith("RSL-ATTENDANCE-") || trimmed.startsWith("RSL-OFFICIAL")) {
            return ParsedQrResult.OfficialReception(trimmed)
        }
        if (trimmed.startsWith("RSL-STUDENT-") || trimmed.startsWith("RSL-")) {
            return ParsedQrResult.StudentBadge(trimmed, "")
        }
        return ParsedQrResult.Generic(trimmed)
    }
}

sealed class ParsedQrResult {
    data class OfficialReception(val token: String) : ParsedQrResult()
    data class StudentBadge(val studentId: String, val studentName: String) : ParsedQrResult()
    data class Generic(val content: String) : ParsedQrResult()
}
