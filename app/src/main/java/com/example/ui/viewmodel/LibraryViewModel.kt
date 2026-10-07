package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Bitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.AppDatabase
import com.example.data.model.AttendanceEntity
import com.example.data.model.LibrarySettingsEntity
import com.example.data.model.PaymentEntity
import com.example.data.model.StudentEntity
import com.example.data.repository.AttendanceResult
import com.example.data.repository.LibraryRepository
import com.example.data.security.SecurityUtils
import com.example.pdf.PdfReportGenerator
import com.example.qr.ParsedQrResult
import com.example.qr.QrCodeGenerator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import java.util.Locale

enum class UserRole {
    NONE, ADMIN, STUDENT
}

enum class AdminTab {
    DASHBOARD, STUDENTS, ATTENDANCE, PAYMENTS, QR_SYSTEM, REPORTS, SETTINGS
}

enum class StudentTab {
    DASHBOARD, SCAN_QR, MY_ATTENDANCE, PAYMENT, MY_PROFILE
}

data class AttendanceUiDialogState(
    val isOpen: Boolean = false,
    val title: String = "",
    val message: String = "",
    val isSuccess: Boolean = true,
    val attendance: AttendanceEntity? = null
)

class LibraryViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: LibraryRepository
    val settingsFlow: StateFlow<LibrarySettingsEntity>
    private val prefs: SharedPreferences = application.getSharedPreferences("rs_library_prefs", Context.MODE_PRIVATE)

    // Auth State
    private val _currentUserRole = MutableStateFlow(UserRole.NONE)
    val currentUserRole: StateFlow<UserRole> = _currentUserRole.asStateFlow()

    private val _loggedInStudent = MutableStateFlow<StudentEntity?>(null)
    val loggedInStudent: StateFlow<StudentEntity?> = _loggedInStudent.asStateFlow()

    // Navigation Tabs
    private val _adminTab = MutableStateFlow(AdminTab.DASHBOARD)
    val adminTab: StateFlow<AdminTab> = _adminTab.asStateFlow()

    private val _studentTab = MutableStateFlow(StudentTab.DASHBOARD)
    val studentTab: StateFlow<StudentTab> = _studentTab.asStateFlow()

    // Sync State
    val isSyncing = MutableStateFlow(false)
    val syncStatusMessage = MutableStateFlow<String?>(null)

    // Student Filter & Search
    val studentSearchQuery = MutableStateFlow("")
    val studentStatusFilter = MutableStateFlow("All") // "All", "Active", "Inactive"

    // Attendance Filter & Date
    val attendanceDateFilter = MutableStateFlow(LibraryRepository.getTodayDate())
    val attendanceSearchQuery = MutableStateFlow("")
    val attendanceMethodFilter = MutableStateFlow("All") // "All", "QR Scan", "Manual"

    // Payment Filters
    val paymentFilterStatus = MutableStateFlow("All") // "All", "Pending", "Verified", "Rejected"
    val paymentSearchQuery = MutableStateFlow("")

    // Dialog & Feedback States
    val attendanceDialogState = MutableStateFlow(AttendanceUiDialogState())
    val selectedStudentForQr = MutableStateFlow<StudentEntity?>(null)
    val generatedPdfFile = MutableStateFlow<File?>(null)
    val statusMessage = MutableStateFlow<String?>(null)

    // Cached Official QR Bitmap
    private val _officialQrBitmap = MutableStateFlow<Bitmap?>(null)
    val officialQrBitmap: StateFlow<Bitmap?> = _officialQrBitmap.asStateFlow()

    // Combined Flow: Students
    val allStudents: StateFlow<List<StudentEntity>>
    val filteredStudents: StateFlow<List<StudentEntity>>

    // Combined Flow: Attendance
    val allAttendance: StateFlow<List<AttendanceEntity>>
    val filteredAttendance: StateFlow<List<AttendanceEntity>>

    // Combined Flow: Payments
    val allPayments: StateFlow<List<PaymentEntity>>
    val filteredPayments: StateFlow<List<PaymentEntity>>
    val studentPayments: StateFlow<List<PaymentEntity>>

    init {
        val database = AppDatabase.getDatabase(application)
        val firestoreManager = com.example.data.firestore.FirestoreManager(
            application,
            database.studentDao(),
            database.attendanceDao(),
            database.librarySettingsDao(),
            database.paymentDao()
        )
        repository = LibraryRepository(
            database.studentDao(),
            database.attendanceDao(),
            database.librarySettingsDao(),
            database.paymentDao(),
            firestoreManager
        )

        allStudents = repository.allStudents.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

        allAttendance = repository.allAttendance.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

        allPayments = repository.allPayments.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

        settingsFlow = repository.settingsFlow.combine(MutableStateFlow(Unit)) { s, _ ->
            s ?: LibrarySettingsEntity()
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            LibrarySettingsEntity()
        )

        filteredStudents = combine(
            allStudents,
            studentSearchQuery,
            studentStatusFilter
        ) { list, query, status ->
            list.filter { student ->
                val matchesQuery = query.isBlank() ||
                        student.fullName.contains(query, ignoreCase = true) ||
                        student.username.contains(query, ignoreCase = true) ||
                        student.id.contains(query, ignoreCase = true) ||
                        student.mobileNumber.contains(query, ignoreCase = true) ||
                        student.seatNumber.contains(query, ignoreCase = true) ||
                        student.course.contains(query, ignoreCase = true)
                val matchesStatus = if (status == "All") true else student.status.equals(status, ignoreCase = true)
                matchesQuery && matchesStatus
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        filteredAttendance = combine(
            allAttendance,
            attendanceDateFilter,
            attendanceSearchQuery,
            attendanceMethodFilter
        ) { list, date, query, method ->
            list.filter { att ->
                val matchesDate = att.date == date
                val matchesQuery = query.isBlank() ||
                        att.studentName.contains(query, ignoreCase = true) ||
                        att.studentId.contains(query, ignoreCase = true)
                val matchesMethod = if (method == "All") true else att.method.equals(method, ignoreCase = true)
                matchesDate && matchesQuery && matchesMethod
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        filteredPayments = combine(
            allPayments,
            paymentFilterStatus,
            paymentSearchQuery
        ) { list, status, query ->
            list.filter { p ->
                val matchesStatus = if (status == "All") true else p.status.equals(status, ignoreCase = true)
                val matchesQuery = query.isBlank() ||
                        p.studentName.contains(query, ignoreCase = true) ||
                        p.studentId.contains(query, ignoreCase = true) ||
                        p.studentUsername.contains(query, ignoreCase = true) ||
                        p.transactionRef.contains(query, ignoreCase = true)
                matchesStatus && matchesQuery
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        studentPayments = combine(
            allPayments,
            _loggedInStudent
        ) { list, student ->
            if (student == null) emptyList()
            else list.filter { it.studentId == student.id }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        // Keep loggedInStudent synced with changes in allStudents
        viewModelScope.launch {
            allStudents.collect { students ->
                val current = _loggedInStudent.value
                if (current != null) {
                    val fresh = students.find { it.id == current.id }
                    if (fresh != null) {
                        _loggedInStudent.value = fresh
                    }
                }
            }
        }

        // Initialize Settings, Cloud listeners, and Restore session
        viewModelScope.launch {
            repository.initializeSystemSettings()
            refreshOfficialQrBitmap()
            restoreDeviceSession()
        }
    }

    private suspend fun restoreDeviceSession() {
        val savedRole = prefs.getString("DEVICE_SESSION_ROLE", null)
        val savedStudentId = prefs.getString("DEVICE_SESSION_STUDENT_ID", null)

        when (savedRole) {
            "ADMIN" -> {
                _currentUserRole.value = UserRole.ADMIN
                _adminTab.value = AdminTab.DASHBOARD
            }
            "STUDENT" -> {
                if (!savedStudentId.isNullOrBlank()) {
                    val student = repository.getStudentById(savedStudentId)
                    if (student != null && student.status.equals("Active", ignoreCase = true)) {
                        _loggedInStudent.value = student
                        _currentUserRole.value = UserRole.STUDENT
                        _studentTab.value = StudentTab.DASHBOARD
                    } else {
                        logout()
                    }
                }
            }
            else -> {
                _currentUserRole.value = UserRole.NONE
            }
        }
    }

    // --- Authentication Actions ---
    fun loginAsAdmin(usernameInput: String, passwordInput: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val trimmedUser = usernameInput.trim()
            val trimmedPass = passwordInput.trim()

            val isUsernameCorrect = trimmedUser == SecurityUtils.ADMIN_USERNAME
            val isPasswordCorrect = SecurityUtils.verifyPassword(trimmedPass, SecurityUtils.ADMIN_PASSWORD_HASH)

            if (isUsernameCorrect && isPasswordCorrect) {
                _currentUserRole.value = UserRole.ADMIN
                _adminTab.value = AdminTab.DASHBOARD
                prefs.edit().putString("DEVICE_SESSION_ROLE", "ADMIN").apply()
                onResult(true, "Admin authentication successful.")
            } else {
                onResult(false, "Invalid Admin username or password.")
            }
        }
    }

    fun loginAsStudent(usernameInput: String, passwordInput: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val trimmedUsername = usernameInput.trim()
            val trimmedPassword = passwordInput.trim()

            if (trimmedUsername.isBlank() || trimmedPassword.isBlank()) {
                onResult(false, "Please enter both User ID and Password.")
                return@launch
            }

            // Find student locally or pull from cloud
            val student = repository.findStudentByIdentifier(trimmedUsername)
            if (student == null) {
                onResult(false, "Student account not found. Please verify your User ID or check with Admin.")
                return@launch
            }

            // Check if student is active
            if (student.status.equals("Inactive", ignoreCase = true)) {
                onResult(false, "Your student account has been deactivated. Please contact the library administrator.")
                return@launch
            }

            // Verify salted SHA-256 password hash
            val isPasswordValid = SecurityUtils.verifyPassword(trimmedPassword, student.passwordHash)
            if (!isPasswordValid) {
                onResult(false, "Incorrect password. Please verify your password.")
                return@launch
            }

            _loggedInStudent.value = student
            _currentUserRole.value = UserRole.STUDENT
            _studentTab.value = StudentTab.DASHBOARD

            // Save persistent session for this student on their individual mobile phone
            prefs.edit()
                .putString("DEVICE_SESSION_ROLE", "STUDENT")
                .putString("DEVICE_SESSION_STUDENT_ID", student.id)
                .apply()

            onResult(true, "Welcome ${student.fullName}")
        }
    }

    fun logout() {
        _currentUserRole.value = UserRole.NONE
        _loggedInStudent.value = null
        _adminTab.value = AdminTab.DASHBOARD
        _studentTab.value = StudentTab.DASHBOARD

        // Clear device session
        prefs.edit().remove("DEVICE_SESSION_ROLE").remove("DEVICE_SESSION_STUDENT_ID").apply()
    }

    // --- Cloud Synchronization Trigger ---
    fun syncWithCloud(onResult: ((Boolean) -> Unit)? = null) {
        viewModelScope.launch {
            isSyncing.value = true
            val success = repository.pullFromCloud()
            refreshOfficialQrBitmap()
            isSyncing.value = false
            syncStatusMessage.value = if (success) "Synchronized with R.S Library Cloud" else "Cloud sync offline (using local storage)"
            onResult?.invoke(success)
        }
    }

    fun onFirebaseAuthSuccess() {
        viewModelScope.launch {
            repository.firestoreManager?.startRealtimeListeners()
            syncStatusMessage.value = "Connected to Firebase Firestore & Auth"
        }
    }

    // --- Student Profile Update (Self-service by Student) ---
    fun updateStudentProfileAndDocuments(
        fullName: String,
        parentName: String,
        mobileNumber: String,
        email: String,
        dob: String,
        address: String,
        course: String,
        emergencyContact: String,
        aadhaarFrontBase64: String?,
        aadhaarBackBase64: String?,
        onResult: (Boolean, String) -> Unit
    ) {
        val current = _loggedInStudent.value
        if (current == null) {
            onResult(false, "No student session active")
            return
        }

        viewModelScope.launch {
            val updated = current.copy(
                fullName = fullName.trim().ifBlank { current.fullName },
                parentName = parentName.trim(),
                mobileNumber = mobileNumber.trim().ifBlank { current.mobileNumber },
                email = email.trim(),
                dob = dob.trim(),
                address = address.trim(),
                course = course.trim().ifBlank { current.course },
                emergencyContact = emergencyContact.trim(),
                aadhaarFrontBase64 = aadhaarFrontBase64 ?: current.aadhaarFrontBase64,
                aadhaarBackBase64 = aadhaarBackBase64 ?: current.aadhaarBackBase64,
                profileCompleted = true
            )
            val success = repository.addOrUpdateStudent(updated)
            if (success) {
                _loggedInStudent.value = updated
                onResult(true, "Profile details and Aadhaar documents updated successfully!")
            } else {
                onResult(false, "Failed to update profile. Please try again.")
            }
        }
    }

    // --- Student Payment Submission ---
    fun submitStudentPayment(
        amount: Double,
        transactionRef: String,
        screenshotBase64: String,
        onResult: (Boolean, String) -> Unit
    ) {
        val student = _loggedInStudent.value
        if (student == null) {
            onResult(false, "No student session active")
            return
        }
        if (amount <= 0.0) {
            onResult(false, "Please enter a valid amount.")
            return
        }
        if (screenshotBase64.isBlank()) {
            onResult(false, "Please upload a payment screenshot.")
            return
        }

        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val payment = PaymentEntity(
                id = "PAY-${now}-${student.id}",
                studentId = student.id,
                studentUsername = student.username,
                studentName = student.fullName,
                amount = amount,
                transactionRef = transactionRef.trim(),
                screenshotBase64 = screenshotBase64,
                date = LibraryRepository.getTodayDate(),
                time = LibraryRepository.getCurrentTime(),
                status = "Pending",
                adminNote = "",
                createdAt = now
            )
            val success = repository.submitPayment(payment)
            if (success) {
                onResult(true, "Payment screenshot submitted! Admin will verify shortly.")
            } else {
                onResult(false, "Error submitting payment proof.")
            }
        }
    }

    // --- Admin Payment Review ---
    fun updatePaymentStatus(
        paymentId: String,
        status: String,
        adminNote: String,
        onResult: (Boolean) -> Unit = {}
    ) {
        viewModelScope.launch {
            val success = repository.updatePaymentStatus(paymentId, status, adminNote)
            onResult(success)
        }
    }

    // --- Admin Paytm QR & Fee Config ---
    fun updatePaytmQrAndFee(
        paytmQrBase64: String?,
        monthlyFee: Double?,
        upiId: String?,
        instructions: String?,
        onResult: (Boolean) -> Unit = {}
    ) {
        viewModelScope.launch {
            val success = repository.updatePaytmQrAndFee(paytmQrBase64, monthlyFee, upiId, instructions)
            onResult(success)
        }
    }

    // --- Student Management (Admin Only) ---
    fun addOrUpdateStudent(
        id: String,
        username: String,
        plainPassword: String?,
        fullName: String,
        parentName: String,
        mobileNumber: String,
        email: String,
        dob: String = "",
        address: String,
        joiningDate: String,
        seatNumber: String,
        course: String,
        emergencyContact: String = "",
        status: String,
        aadhaarFrontBase64: String? = null,
        aadhaarBackBase64: String? = null,
        existingPasswordHash: String? = null,
        onResult: (Boolean, String) -> Unit
    ) {
        viewModelScope.launch {
            val trimmedId = id.trim()
            val trimmedUsername = username.trim().lowercase(Locale.getDefault())

            if (trimmedId.isBlank() || trimmedUsername.isBlank() || fullName.isBlank()) {
                onResult(false, "Student ID, Username, and Full Name are required.")
                return@launch
            }

            // Calculate password hash
            val finalHash = when {
                !plainPassword.isNullOrBlank() -> SecurityUtils.hashPassword(plainPassword.trim())
                !existingPasswordHash.isNullOrBlank() -> existingPasswordHash
                else -> {
                    onResult(false, "Password is required for student login account.")
                    return@launch
                }
            }

            val student = StudentEntity(
                id = trimmedId,
                username = trimmedUsername,
                passwordHash = finalHash,
                fullName = fullName.trim(),
                parentName = parentName.trim(),
                mobileNumber = mobileNumber.trim(),
                email = email.trim(),
                dob = dob.trim(),
                address = address.trim(),
                joiningDate = joiningDate.trim(),
                seatNumber = seatNumber.trim(),
                course = course.trim(),
                emergencyContact = emergencyContact.trim(),
                status = status,
                aadhaarFrontBase64 = aadhaarFrontBase64,
                aadhaarBackBase64 = aadhaarBackBase64,
                profileCompleted = (!aadhaarFrontBase64.isNullOrBlank() && dob.isNotBlank()),
                studentQrToken = "RSL-STUDENT-$trimmedId"
            )

            val success = repository.addOrUpdateStudent(student)
            if (success) {
                onResult(true, "Student account successfully saved.")
            } else {
                onResult(false, "Failed to save student account.")
            }
        }
    }

    fun addOrUpdateStudent(
        student: StudentEntity,
        newPasswordIfAny: String?,
        isEdit: Boolean,
        onResult: (Boolean, String) -> Unit
    ) {
        viewModelScope.launch {
            val trimmedId = student.id.trim()
            val trimmedUsername = student.username.trim().lowercase(Locale.getDefault())

            if (trimmedId.isBlank() || trimmedUsername.isBlank() || student.fullName.isBlank()) {
                onResult(false, "Student ID, Username, and Full Name are required.")
                return@launch
            }

            val finalHash = when {
                !newPasswordIfAny.isNullOrBlank() -> SecurityUtils.hashPassword(newPasswordIfAny.trim())
                student.passwordHash.isNotBlank() -> student.passwordHash
                else -> {
                    onResult(false, "Password is required for student login account.")
                    return@launch
                }
            }

            val toSave = student.copy(
                id = trimmedId,
                username = trimmedUsername,
                passwordHash = finalHash,
                studentQrToken = if (student.studentQrToken.isBlank()) "RSL-STUDENT-$trimmedId" else student.studentQrToken
            )
            val success = repository.addOrUpdateStudent(toSave)
            if (success) {
                onResult(true, if (isEdit) "Student updated successfully" else "Student added successfully")
            } else {
                onResult(false, "Failed to save student account.")
            }
        }
    }

    fun deleteStudent(studentId: String, onResult: (Boolean) -> Unit = {}) {
        viewModelScope.launch {
            val success = repository.deleteStudent(studentId)
            onResult(success)
        }
    }

    fun deleteStudent(student: StudentEntity, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            repository.deleteStudent(student.id)
            onComplete()
        }
    }

    fun resetStudentPassword(studentId: String, newPass: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val student = repository.getStudentById(studentId)
            if (student == null) {
                onResult(false, "Student account not found")
                return@launch
            }
            val newHash = SecurityUtils.hashPassword(newPass.trim())
            val updated = student.copy(passwordHash = newHash)
            val success = repository.addOrUpdateStudent(updated)
            onResult(success, if (success) "Password successfully updated" else "Failed to update password")
        }
    }

    fun generateNextStudentId(): String {
        val count = allStudents.value.size + 1
        return String.format(Locale.getDefault(), "RSL-%03d", count)
    }

    // --- QR Code Actions ---
    fun generateNewOfficialQr(onComplete: (String) -> Unit = {}) {
        viewModelScope.launch {
            val newToken = repository.generateNewOfficialQrToken()
            refreshOfficialQrBitmap()
            onComplete(newToken)
        }
    }

    fun toggleQrActive(isActive: Boolean) {
        viewModelScope.launch {
            repository.setQrActiveStatus(isActive)
        }
    }

    fun refreshOfficialQrBitmap() {
        viewModelScope.launch {
            val settings = repository.getSettings()
            val bitmap = QrCodeGenerator.generateQrBitmap(
                content = settings.officialQrToken,
                width = 512,
                height = 512
            )
            _officialQrBitmap.value = bitmap
        }
    }

    // --- Attendance Marking ---
    fun markAttendanceFromQr(scannedRaw: String) {
        viewModelScope.launch {
            val student = _loggedInStudent.value
            if (student == null) {
                attendanceDialogState.value = AttendanceUiDialogState(
                    isOpen = true,
                    title = "Session Error",
                    message = "No logged-in student session found. Please log in again.",
                    isSuccess = false
                )
                return@launch
            }

            val token = when (val parsed = QrCodeGenerator.parseQrCode(scannedRaw)) {
                is com.example.qr.ParsedQrResult.OfficialReception -> parsed.token
                is com.example.qr.ParsedQrResult.Generic -> parsed.content
                else -> scannedRaw.trim()
            }
            val result = repository.verifyAndRecordQrAttendance(
                scannedToken = token,
                studentId = student.id
            )

            when (result) {
                is AttendanceResult.Success -> {
                    attendanceDialogState.value = AttendanceUiDialogState(
                        isOpen = true,
                        title = "Attendance Marked! 🎉",
                        message = "Welcome, ${student.fullName}!\nTime: ${result.attendance.checkInTime} on ${result.attendance.date}",
                        isSuccess = true,
                        attendance = result.attendance
                    )
                }
                is AttendanceResult.Duplicate -> {
                    attendanceDialogState.value = AttendanceUiDialogState(
                        isOpen = true,
                        title = "Already Marked Today",
                        message = "Your attendance for today was already recorded at ${result.existing.checkInTime}.",
                        isSuccess = false,
                        attendance = result.existing
                    )
                }
                is AttendanceResult.InvalidQr -> {
                    attendanceDialogState.value = AttendanceUiDialogState(
                        isOpen = true,
                        title = "Invalid QR Code",
                        message = result.message,
                        isSuccess = false
                    )
                }
                is AttendanceResult.InactiveStudent -> {
                    attendanceDialogState.value = AttendanceUiDialogState(
                        isOpen = true,
                        title = "Account Inactive",
                        message = result.message,
                        isSuccess = false
                    )
                }
                is AttendanceResult.StudentNotFound -> {
                    attendanceDialogState.value = AttendanceUiDialogState(
                        isOpen = true,
                        title = "Student Not Found",
                        message = result.message,
                        isSuccess = false
                    )
                }
                is AttendanceResult.Error -> {
                    attendanceDialogState.value = AttendanceUiDialogState(
                        isOpen = true,
                        title = "Attendance Error",
                        message = result.message,
                        isSuccess = false
                    )
                }
            }
        }
    }

    fun markManualAttendance(
        student: StudentEntity,
        status: String = "Present",
        customDate: String? = null,
        onResult: (AttendanceResult) -> Unit = {}
    ) {
        viewModelScope.launch {
            val result = repository.markAttendance(
                student = student,
                method = "Manual",
                status = status,
                customDate = customDate
            )
            onResult(result)
        }
    }

    fun dismissAttendanceDialog() {
        attendanceDialogState.value = AttendanceUiDialogState(isOpen = false)
    }

    fun markManualAttendance(
        studentId: String,
        date: String,
        time: String,
        status: String,
        onResult: (Boolean, String) -> Unit
    ) {
        viewModelScope.launch {
            val student = repository.getStudentById(studentId)
            if (student == null) {
                onResult(false, "Student not found")
                return@launch
            }
            val res = repository.markAttendance(
                student = student,
                method = "Manual",
                status = status,
                customDate = date,
                customTime = time
            )
            when (res) {
                is AttendanceResult.Success -> onResult(true, "Attendance recorded")
                is AttendanceResult.Duplicate -> onResult(false, "Attendance already recorded today")
                else -> onResult(false, "Failed to record attendance")
            }
        }
    }

    fun markCheckOut(attendanceId: Long) {
        viewModelScope.launch {
            repository.markCheckOut(attendanceId)
        }
    }

    fun deleteAttendanceRecord(attendanceId: Long) {
        viewModelScope.launch {
            repository.deleteAttendance(attendanceId)
        }
    }

    fun regenerateOfficialQrToken(onComplete: (String) -> Unit = {}) {
        generateNewOfficialQr(onComplete)
    }

    fun updateLibrarySettings(settings: LibrarySettingsEntity, onComplete: (Boolean) -> Unit = {}) {
        viewModelScope.launch {
            val success = repository.updateSettings(settings)
            onComplete(success)
        }
    }

    fun exportAllStudentsPdf(onComplete: (File?) -> Unit) {
        viewModelScope.launch {
            val file = PdfReportGenerator.generateAllStudentsPdf(
                context = getApplication(),
                settings = repository.getSettings(),
                students = allStudents.value
            )
            generatedPdfFile.value = file
            onComplete(file)
        }
    }

    fun exportAttendanceReportPdf(date: String, onComplete: (File?) -> Unit) {
        viewModelScope.launch {
            val records = allAttendance.value.filter { it.date == date }
            val file = PdfReportGenerator.generateAttendanceReportPdf(
                context = getApplication(),
                settings = repository.getSettings(),
                date = date,
                records = records
            )
            generatedPdfFile.value = file
            onComplete(file)
        }
    }

    fun exportIndividualStudentPdf(student: StudentEntity, onComplete: (File?) -> Unit) {
        viewModelScope.launch {
            val records = allAttendance.value.filter { it.studentId == student.id }
            val file = PdfReportGenerator.generateIndividualStudentPdf(
                context = getApplication(),
                settings = repository.getSettings(),
                student = student,
                records = records
            )
            generatedPdfFile.value = file
            onComplete(file)
        }
    }

    fun markAttendanceFromQr(scannedRaw: String, studentId: String) {
        markAttendanceFromQr(scannedRaw)
    }

    // --- Reports ---
    fun generateDailyReportPdf(date: String, onComplete: (File?) -> Unit) {
        exportAttendanceReportPdf(date, onComplete)
    }

    fun generateMonthlyStudentReportPdf(studentId: String, monthYear: String, onComplete: (File?) -> Unit) {
        viewModelScope.launch {
            val student = repository.getStudentById(studentId)
            if (student == null) {
                onComplete(null)
                return@launch
            }
            exportIndividualStudentPdf(student, onComplete)
        }
    }

    // --- Helper DAO Access ---
    private suspend fun LibraryRepository.attendanceDao_getAttendanceForDate(date: String): List<AttendanceEntity> {
        val all = allAttendance.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList()).value
        return all.filter { it.date == date }
    }

    private suspend fun LibraryRepository.attendanceDao_getAttendanceForStudent(studentId: String): List<AttendanceEntity> {
        val all = allAttendance.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList()).value
        return all.filter { it.studentId == studentId }
    }

    // Navigation setters
    fun setAdminTab(tab: AdminTab) { _adminTab.value = tab }
    fun setStudentTab(tab: StudentTab) { _studentTab.value = tab }
}
