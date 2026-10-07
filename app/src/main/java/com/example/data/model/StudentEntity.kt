package com.example.data.model

import androidx.annotation.Keep
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Keep
@Entity(
    tableName = "students",
    indices = [
        Index(value = ["username"], unique = true),
        Index(value = ["mobileNumber"])
    ]
)
data class StudentEntity(
    @PrimaryKey
    val id: String = "", // e.g. "RSL-001"
    val username: String = "", // Individual unique student username e.g. "student001"
    val passwordHash: String = "", // Securely salted SHA-256 hash
    val fullName: String = "",
    val parentName: String = "",
    val mobileNumber: String = "",
    val email: String = "",
    val dob: String = "", // YYYY-MM-DD
    val address: String = "",
    val joiningDate: String = "", // YYYY-MM-DD
    val seatNumber: String = "", // e.g. "Seat A-12"
    val course: String = "", // e.g. "UPSC / Civil Services"
    val emergencyContact: String = "",
    val photoUri: String? = null,
    val aadhaarFrontBase64: String? = null,
    val aadhaarBackBase64: String? = null,
    val profileCompleted: Boolean = false,
    val status: String = "Active", // "Active" or "Inactive"
    val studentQrToken: String = "RSL-STUDENT-$id",
    val createdAt: Long = System.currentTimeMillis()
)
