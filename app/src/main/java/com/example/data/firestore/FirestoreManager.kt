package com.example.data.firestore

import android.content.Context
import android.util.Log
import com.example.R
import com.example.data.db.AttendanceDao
import com.example.data.db.LibrarySettingsDao
import com.example.data.db.PaymentDao
import com.example.data.db.StudentDao
import com.example.data.model.AttendanceEntity
import com.example.data.model.LibrarySettingsEntity
import com.example.data.model.PaymentEntity
import com.example.data.model.StudentEntity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class FirestoreManager(
    private val context: Context,
    private val studentDao: StudentDao,
    private val attendanceDao: AttendanceDao,
    private val settingsDao: LibrarySettingsDao,
    private val paymentDao: PaymentDao
) {
    private val TAG = "FirestoreManager"

    // MANDATORY: Always obtain custom database instance from string resource
    val firestore: FirebaseFirestore by lazy {
        val databaseId = context.getString(R.string.firestore_database_id)
        FirebaseFirestore.getInstance(databaseId)
    }

    private var studentsListener: ListenerRegistration? = null
    private var attendanceListener: ListenerRegistration? = null
    private var settingsListener: ListenerRegistration? = null
    private var paymentsListener: ListenerRegistration? = null

    private val scope = CoroutineScope(Dispatchers.IO)

    /**
     * Starts realtime listeners for students, attendance, settings, and payments.
     * Keeps local Room database reactive and synchronized with Firestore.
     */
    fun startRealtimeListeners() {
        val auth = FirebaseAuth.getInstance()
        if (auth.currentUser == null) {
            Log.d(TAG, "Auth currentUser is null; waiting for auth before attaching listeners")
            return
        }

        stopRealtimeListeners()

        // 1. Students collection listener
        val studentsPath = "students"
        studentsListener = firestore.collection(studentsPath)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    handleFirestoreError(error, OperationType.LIST, studentsPath)
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    scope.launch {
                        try {
                            val remoteStudents = snapshot.documents.mapNotNull { doc ->
                                val student = doc.toObject(StudentEntity::class.java)
                                student?.copy(id = doc.id)
                            }
                            if (remoteStudents.isNotEmpty()) {
                                studentDao.insertAllStudents(remoteStudents)
                            }
                            
                            }
                        } catch (e: Exception) {
                            Log.e(TAG, "Error syncing students to Room: ${e.message}")
                        }
                    }
                }
            }

        // 2. Attendance collection listener
        val attendancePath = "attendance"
        attendanceListener = firestore.collection(attendancePath)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    handleFirestoreError(error, OperationType.LIST, attendancePath)
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    scope.launch {
                        try {
                            val remoteList = snapshot.documents.mapNotNull { doc ->
                                doc.toObject(AttendanceEntity::class.java)
                            }
                            if (remoteList.isNotEmpty()) {
                                attendanceDao.insertAllAttendance(remoteList)
                            }
                        } catch (e: Exception) {
                            Log.e(TAG, "Error syncing attendance to Room: ${e.message}")
                        }
                    }
                }
            }

        // 3. Settings document listener
        val settingsDocPath = "settings/library_config"
        settingsListener = firestore.document(settingsDocPath)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    handleFirestoreError(error, OperationType.GET, settingsDocPath)
                    return@addSnapshotListener
                }

                if (snapshot != null && snapshot.exists()) {
                    scope.launch {
                        try {
                            val remoteSettings = snapshot.toObject(LibrarySettingsEntity::class.java)
                            if (remoteSettings != null) {
                                settingsDao.insertOrUpdate(remoteSettings)
                            }
                        } catch (e: Exception) {
                            Log.e(TAG, "Error syncing settings to Room: ${e.message}")
                        }
                    }
                }
            }

        // 4. Payments collection listener
        val paymentsPath = "payments"
        paymentsListener = firestore.collection(paymentsPath)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    handleFirestoreError(error, OperationType.LIST, paymentsPath)
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    scope.launch {
                        try {
                            val remotePayments = snapshot.documents.mapNotNull { doc ->
                                val payment = doc.toObject(PaymentEntity::class.java)
                                payment?.copy(id = doc.id)
                            }
                            if (remotePayments.isNotEmpty()) {
                                paymentDao.insertAllPayments(remotePayments)
                            }
                        } catch (e: Exception) {
                            Log.e(TAG, "Error syncing payments to Room: ${e.message}")
                        }
                    }
                }
            }
    }

    fun stopRealtimeListeners() {
        studentsListener?.remove()
        studentsListener = null
        attendanceListener?.remove()
        attendanceListener = null
        settingsListener?.remove()
        settingsListener = null
        paymentsListener?.remove()
        paymentsListener = null
    }

    suspend fun saveStudent(student: StudentEntity): Boolean {
        return try {
            firestore.collection("students").document(student.id)
                .set(student, SetOptions.merge())
                .await()
            true
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, "students/${student.id}")
            false
        }
    }

    suspend fun deleteStudent(studentId: String): Boolean {
        return try {
            firestore.collection("students").document(studentId)
                .delete()
                .await()
            true
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, "students/$studentId")
            false
        }
    }

    suspend fun saveAttendance(attendance: AttendanceEntity): Boolean {
        return try {
            val docId = "${attendance.studentId}_${attendance.date}"
            firestore.collection("attendance").document(docId)
                .set(attendance, SetOptions.merge())
                .await()
            true
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, "attendance")
            false
        }
    }

    suspend fun savePayment(payment: PaymentEntity): Boolean {
        return try {
            firestore.collection("payments").document(payment.id)
                .set(payment, SetOptions.merge())
                .await()
            true
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, "payments/${payment.id}")
            false
        }
    }

    suspend fun updatePaymentStatus(
        paymentId: String,
        status: String,
        adminNote: String,
        verifiedAt: String
    ): Boolean {
        return try {
            val updates = mapOf(
                "status" to status,
                "adminNote" to adminNote,
                "verifiedAt" to verifiedAt
            )
            firestore.collection("payments").document(paymentId)
                .update(updates)
                .await()
            true
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, "payments/$paymentId")
            false
        }
    }

    suspend fun saveSettings(settings: LibrarySettingsEntity): Boolean {
        return try {
            firestore.collection("settings").document("library_config")
                .set(settings, SetOptions.merge())
                .await()
            true
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, "settings/library_config")
            false
        }
    }

    suspend fun fetchStudentByUsername(username: String): StudentEntity? {
        val path = "students"
        return try {
            val snapshot = firestore.collection(path)
                .whereEqualTo("username", username)
                .limit(1)
                .get()
                .await()
            snapshot.documents.firstOrNull()?.toObject(StudentEntity::class.java)
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.GET, path)
            null
        }
    }

    suspend fun fetchStudentById(id: String): StudentEntity? {
        val path = "students/$id"
        return try {
            val doc = firestore.collection("students").document(id).get().await()
            if (doc.exists()) doc.toObject(StudentEntity::class.java) else null
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.GET, path)
            null
        }
    }
}
