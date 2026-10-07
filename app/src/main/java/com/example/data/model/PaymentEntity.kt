package com.example.data.model

import androidx.annotation.Keep
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Keep
@Entity(
    tableName = "payments",
    indices = [
        Index(value = ["studentId"]),
        Index(value = ["status"]),
        Index(value = ["createdAt"])
    ]
)
data class PaymentEntity(
    @PrimaryKey
    val id: String = "", // e.g. "PAY-1728212345-RSL-001"
    val studentId: String = "",
    val studentUsername: String = "",
    val studentName: String = "",
    val amount: Double = 0.0,
    val transactionRef: String = "", // UPI UTR or reference ID
    val screenshotBase64: String = "", // Compressed Base64 image
    val date: String = "", // YYYY-MM-DD
    val time: String = "", // HH:mm format
    val status: String = "Pending", // "Pending", "Verified", "Rejected"
    val adminNote: String = "",
    val verifiedAt: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
