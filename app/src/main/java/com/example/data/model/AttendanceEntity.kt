package com.example.data.model

import androidx.annotation.Keep
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Keep
@Entity(
    tableName = "attendance",
    indices = [
        Index(value = ["studentId", "date"], unique = true),
        Index(value = ["date"]),
        Index(value = ["studentId"])
    ]
)
data class AttendanceEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val studentId: String = "",
    val studentName: String = "",
    val date: String = "", // format YYYY-MM-DD
    val checkInTime: String = "", // e.g. "09:15 AM"
    val checkOutTime: String? = null,
    val status: String = "Present", // "Present", "Late", "Excused"
    val method: String = "QR Scan", // "QR Scan" or "Manual"
    val remarks: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
