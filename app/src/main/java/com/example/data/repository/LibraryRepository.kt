package com.example.data.repository

import com.example.data.db.AttendanceDao
import com.example.data.db.LibrarySettingsDao
import com.example.data.db.PaymentDao
import com.example.data.db.StudentDao
import com.example.data.firestore.FirestoreManager
import com.example.data.model.AttendanceEntity
import com.example.data.model.LibrarySettingsEntity
import com.example.data.model.PaymentEntity
import com.example.data.model.StudentEntity
import com.example.data.security.SecurityUtils
import com.example.data.sync.CloudSyncManager
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

sealed class AttendanceResult {
    data class Success(val message: String, val attendance: AttendanceEntity) : AttendanceResult()
    data class Duplicate(val message: String, val existing: AttendanceEntity) : AttendanceResult()
    data class StudentNotFound(val message: String) : AttendanceResult()
    data class InactiveStudent(val message: String) : AttendanceResult()
    data class InvalidQr(val message: String) : AttendanceResult()
    data class Error(val message: String) : AttendanceResult()
}

class LibraryRepository(
    private val studentDao: StudentDao,
    private val attendanceDao: AttendanceDao,
    private val settingsDao: LibrarySettingsDao,
    private val paymentDao: PaymentDao,
    val firestoreManager: FirestoreManager? = null
) {
    val allStudents: Flow<List<StudentEntity>> = studentDao.getAllStudents()
    val activeStudents: Flow<List<StudentEntity>> = studentDao.getActiveStudents()
    val totalStudentsCount: Flow<Int> = studentDao.getTotalStudentsCount()
    val activeStudentsCount: Flow<Int> = studentDao.getActiveStudentsCount()

    val allAttendance: Flow<List<AttendanceEntity>> = attendanceDao.getAllAttendance()
    val totalAttendanceCount: Flow<Int> = attendanceDao.getTotalAttendanceCount()
    val settingsFlow: Flow<LibrarySettingsEntity?> = settingsDao.getSettingsFlow()

    // Payments flows
    val allPayments: Flow<List<PaymentEntity>> = paymentDao.getAllPayments()

    fun getPaymentsForStudent(studentId: String): Flow<List<PaymentEntity>> =
        paymentDao.getPaymentsByStudent(studentId)

    fun getAttendanceForDate(date: String): Flow<List<AttendanceEntity>> =
        attendanceDao.getAttendanceByDate(date)

    fun getAttendanceForStudent(studentId: String): Flow<List<AttendanceEntity>> =
        attendanceDao.getAttendanceForStudent(studentId)

    fun getPresentCountForDate(date: String): Flow<Int> =
        attendanceDao.getPresentCountForDate(date)

    fun getStudentPresentCount(studentId: String): Flow<Int> =
        attendanceDao.getStudentPresentCount(studentId)

    fun searchStudents(query: String): Flow<List<StudentEntity>> =
        studentDao.searchStudents(query)

    suspend fun getStudentById(id: String): StudentEntity? =
        studentDao.getStudentById(id)

    suspend fun getStudentByUsername(username: String): StudentEntity? =
        studentDao.getStudentByUsername(username)

    suspend fun findStudentByIdentifier(identifier: String): StudentEntity? {
        val trimmed = identifier.trim()
        val direct = studentDao.findStudentByIdentifier(trimmed)
        if (direct != null) return direct

        // Pull latest state from cloud if student was registered from another phone
        pullFromCloud()
        val retry = studentDao.findStudentByIdentifier(trimmed)
        if (retry != null) return retry

        // Try direct Firestore lookup
        val remote = firestoreManager?.fetchStudentByUsername(trimmed)
            ?: firestoreManager?.fetchStudentById(trimmed)
        if (remote != null) {
            studentDao.insertStudent(remote)
            return remote
        }
        return null
    }

    suspend fun addOrUpdateStudent(student: StudentEntity): Boolean {
    return try {
        studentDao.insertStudent(student)

        suspend fun addOrUpdateStudent(student: StudentEntity): Boolean {
    return try {
        studentDao.insertStudent(student)
        try {
            firestoreManager?.saveStudent(student)
        } catch (_: Exception) {}
        try {
            triggerPush()
        } catch (_: Exception) {}
        true
    } catch (e: Exception) {
        false
    }
}

        triggerPush()
        true
    } catch (e: Exception) {
        false
    }
}

    suspend fun deleteStudent(studentId: String): Boolean {
        studentDao.deleteStudentById(studentId)
        firestoreManager?.deleteStudent(studentId)
        triggerPush()
        return true
    }

    suspend fun getSettings(): LibrarySettingsEntity {
        return settingsDao.getSettings() ?: LibrarySettingsEntity().also {
            settingsDao.insertOrUpdate(it)
        }
    }

    suspend fun updateSettings(settings: LibrarySettingsEntity): Boolean {
        settingsDao.insertOrUpdate(settings)
        firestoreManager?.saveSettings(settings)
        triggerPush()
        return true
    }

    suspend fun updatePaytmQrAndFee(
        paytmQrBase64: String?,
        monthlyFee: Double?,
        upiId: String?,
        instructions: String?
    ): Boolean {
        val current = getSettings()
        val updated = current.copy(
            paytmQrCodeBase64 = paytmQrBase64 ?: current.paytmQrCodeBase64,
            monthlyFee = monthlyFee ?: current.monthlyFee,
            upiId = upiId ?: current.upiId,
            paymentInstructions = instructions ?: current.paymentInstructions
        )
        return updateSettings(updated)
    }

    suspend fun submitPayment(payment: PaymentEntity): Boolean {
        paymentDao.insertPayment(payment)
        firestoreManager?.savePayment(payment)
        return true
    }

    suspend fun updatePaymentStatus(
        paymentId: String,
        status: String,
        adminNote: String
    ): Boolean {
        val verifiedAt = SimpleDateFormat("yyyy-MM-dd hh:mm a", Locale.getDefault()).format(Date())
        paymentDao.updateStatus(paymentId, status, adminNote, verifiedAt)
        firestoreManager?.updatePaymentStatus(paymentId, status, adminNote, verifiedAt)
        return true
    }

    suspend fun generateNewOfficialQrToken(): String {
        val newToken = "RSL-" + UUID.randomUUID().toString().take(12).uppercase()
        val currentSettings = getSettings()
        val updated = currentSettings.copy(
            officialQrToken = newToken,
            isQrActive = true,
            qrGeneratedDate = getTodayDate()
        )
        updateSettings(updated)
        return newToken
    }

    suspend fun setQrActiveStatus(isActive: Boolean) {
        val currentSettings = getSettings()
        updateSettings(currentSettings.copy(isQrActive = isActive))
    }

    suspend fun verifyAndRecordQrAttendance(
        scannedToken: String,
        studentId: String
    ): AttendanceResult {
        val trimmedToken = scannedToken.trim()
        val currentSettings = getSettings()

        if (!currentSettings.isQrActive) {
            return AttendanceResult.InvalidQr("Attendance QR is currently inactive. Contact Administrator.")
        }

        if (trimmedToken != currentSettings.officialQrToken) {
            return AttendanceResult.InvalidQr("Invalid QR Code. Please scan the official R.S Library QR Code.")
        }

        val student = studentDao.getStudentById(studentId)
            ?: return AttendanceResult.StudentNotFound("Student record not found for ID: $studentId")

        if (student.status != "Active") {
            return AttendanceResult.InactiveStudent("Student account is inactive. Please contact Library Admin.")
        }

        return markAttendance(
            student = student,
            method = "QR Scan",
            status = "Present"
        )
    }

    suspend fun markAttendance(
        student: StudentEntity,
        method: String = "Manual",
        status: String = "Present",
        customDate: String? = null,
        customTime: String? = null
    ): AttendanceResult {
        if (student.status != "Active") {
            return AttendanceResult.InactiveStudent("Student account is inactive.")
        }

        val date = customDate ?: getTodayDate()
        val time = customTime ?: getCurrentTime()

        // 4. Duplicate Check: rule enforces 1 attendance entry per student per day
        val existing = attendanceDao.getTodayAttendanceForStudent(student.id, date)
        if (existing != null) {
            return AttendanceResult.Duplicate(
                "Your attendance has already been marked today.",
                existing
            )
        }

        // 5. Insert new attendance record
        val attendance = AttendanceEntity(
            studentId = student.id,
            studentName = student.fullName,
            date = date,
            checkInTime = time,
            checkOutTime = null,
            status = status,
            method = method
        )
        val insertedId = attendanceDao.insertAttendance(attendance)
        val created = attendance.copy(id = insertedId)

        // Save in Firestore directly
        firestoreManager?.saveAttendance(created)
        triggerPush()

        return AttendanceResult.Success(
            "Attendance Marked Successfully.",
            created
        )
    }

    suspend fun markCheckOut(attendanceId: Long) {
        val time = getCurrentTime()
        attendanceDao.markCheckOut(attendanceId, time)
        val att = attendanceDao.getAttendanceById(attendanceId)
        if (att != null) {
            firestoreManager?.saveAttendance(att)
        }
        triggerPush()
    }

    suspend fun deleteAttendance(attendanceId: Long) {
        attendanceDao.deleteAttendanceById(attendanceId)
        triggerPush()
    }

    suspend fun pullFromCloud(): Boolean {
        return CloudSyncManager.pullFromCloud(studentDao, attendanceDao, settingsDao)
    }

    private suspend fun triggerPush() {
        try {
            val students = studentDao.getAllStudentsList()
            val attendance = attendanceDao.getAllAttendanceList()
            val settings = getSettings()
            CloudSyncManager.pushToCloud(students, attendance, settings)
        } catch (_: Exception) {
            // Non-blocking
        }
    }

    suspend fun initializeSystemSettings() {
        if (settingsDao.getSettings() == null) {
            settingsDao.insertOrUpdate(LibrarySettingsEntity())
        }
        firestoreManager?.startRealtimeListeners()
        pullFromCloud()
    }

    companion object {
        fun getTodayDate(): String {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            return sdf.format(Date())
        }

        fun getCurrentTime(): String {
            val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
            return sdf.format(Date())
        }
    }
}
