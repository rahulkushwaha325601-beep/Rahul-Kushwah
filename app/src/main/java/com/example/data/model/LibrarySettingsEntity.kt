package com.example.data.model

import androidx.annotation.Keep
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.security.SecurityUtils

@Keep
@Entity(tableName = "library_settings")
data class LibrarySettingsEntity(
    @PrimaryKey
    val id: Int = 1,
    val libraryName: String = "R.S Library",
    val tagline: String = "Study • Focus • Succeed",
    val ownerName: String = "Library Administrator",
    val contactPhone: String = "+91 98765 43210",
    val contactEmail: String = "admin@rslibrary.com",
    val address: String = "Plot 42, Knowledge Park, Main Road",
    val totalSeats: Int = 100,
    val adminUsername: String = SecurityUtils.ADMIN_USERNAME, // "Raman1998"
    val adminPasswordHash: String = SecurityUtils.ADMIN_PASSWORD_HASH, // Salted SHA-256 of "Rahul328650@#"
    val officialQrToken: String = "RSL-OFFICIAL-RECEPTION-2026",
    val isQrActive: Boolean = true,
    val qrGeneratedDate: String = "2026-10-06",
    val openingTime: String = "07:00 AM",
    val closingTime: String = "11:00 PM",
    val paytmQrCodeBase64: String? = null,
    val monthlyFee: Double = 600.0,
    val upiId: String = "raman.library@paytm",
    val paymentInstructions: String = "Scan the R.S Library Paytm QR Code above using Paytm, PhonePe, Google Pay, or any UPI app. After payment, upload the payment screenshot below along with the transaction reference for verification."
)
