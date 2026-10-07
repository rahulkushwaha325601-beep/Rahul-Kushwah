package com.example.data.sync

import android.util.Log
import com.example.data.db.AttendanceDao
import com.example.data.db.LibrarySettingsDao
import com.example.data.db.StudentDao
import com.example.data.model.AttendanceEntity
import com.example.data.model.LibrarySettingsEntity
import com.example.data.model.StudentEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

object CloudSyncManager {

    private const val TAG = "CloudSyncManager"
    private const val SYNC_OBJECT_ID = "ff808181a09d98f701a1112b886c092f"
    private const val API_BASE_URL = "https://api.restful-api.dev/objects/$SYNC_OBJECT_ID"

    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .writeTimeout(10, TimeUnit.SECONDS)
        .build()

    private val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()

    /**
     * Pulls latest student accounts, attendance, and official QR settings from the cloud.
     * Merges into local Room database so student devices can authenticate accounts created by Admin.
     */
    suspend fun pullFromCloud(
        studentDao: StudentDao,
        attendanceDao: AttendanceDao,
        settingsDao: LibrarySettingsDao
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url(API_BASE_URL)
                .get()
                .build()

            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                Log.w(TAG, "Pull failed with HTTP code ${response.code}")
                return@withContext false
            }

            val bodyString = response.body?.string() ?: return@withContext false
            val root = JSONObject(bodyString)
            if (!root.has("data")) return@withContext false

            val data = root.getJSONObject("data")

            // 1. Sync Students
            if (data.has("students")) {
                val studentsArr = data.getJSONArray("students")
                val studentsList = mutableListOf<StudentEntity>()
                for (i in 0 until studentsArr.length()) {
                    val obj = studentsArr.getJSONObject(i)
                    studentsList.add(
                        StudentEntity(
                            id = obj.getString("id"),
                            username = obj.getString("username"),
                            passwordHash = obj.getString("passwordHash"),
                            fullName = obj.getString("fullName"),
                            parentName = obj.optString("parentName", ""),
                            mobileNumber = obj.optString("mobileNumber", ""),
                            email = obj.optString("email", ""),
                            address = obj.optString("address", ""),
                            joiningDate = obj.optString("joiningDate", ""),
                            seatNumber = obj.optString("seatNumber", ""),
                            course = obj.optString("course", ""),
                            status = obj.optString("status", "Active"),
                            studentQrToken = obj.optString("studentQrToken", "RSL-STUDENT-${obj.getString("id")}"),
                            createdAt = obj.optLong("createdAt", System.currentTimeMillis())
                        )
                    )
                }
                val localStudents = studentDao.getAllStudentsList()
val localMap = localStudents.associateBy { it.id }.toMutableMap()

// Server se aayi list merge karein, lekin jo local me naye hain unhe preserve rakhein
for (remote in studentsList) {
    if (!localMap.containsKey(remote.id)) {
        localMap[remote.id] = remote
    }
}
studentDao.insertAllStudents(localMap.values.toList())
                
            }

            // 2. Sync Attendance
            if (data.has("attendance")) {
                val attArr = data.getJSONArray("attendance")
                val attendanceList = mutableListOf<AttendanceEntity>()
                for (i in 0 until attArr.length()) {
                    val obj = attArr.getJSONObject(i)
                    attendanceList.add(
                        AttendanceEntity(
                            id = obj.optLong("id", 0),
                            studentId = obj.getString("studentId"),
                            studentName = obj.getString("studentName"),
                            date = obj.getString("date"),
                            checkInTime = obj.getString("checkInTime"),
                            checkOutTime = if (obj.isNull("checkOutTime")) null else obj.getString("checkOutTime"),
                            status = obj.optString("status", "Present"),
                            method = obj.optString("method", "QR Scan"),
                            remarks = obj.optString("remarks", ""),
                            timestamp = obj.optLong("timestamp", System.currentTimeMillis())
                        )
                    )
                }
                if (attendanceList.isNotEmpty()) {
                    attendanceDao.insertAllAttendance(attendanceList)
                }
            }

            // 3. Sync Official QR Token
            if (data.has("officialQrToken")) {
                val qrToken = data.getString("officialQrToken")
                val isQrActive = data.optBoolean("isQrActive", true)
                val currentSettings = settingsDao.getSettings() ?: LibrarySettingsEntity()
                settingsDao.insertOrUpdate(
                    currentSettings.copy(
                        officialQrToken = qrToken,
                        isQrActive = isQrActive
                    )
                )
            }

            Log.i(TAG, "Pull completed successfully")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Pull error: ${e.localizedMessage}")
            false
        }
    }

    /**
     * Pushes current student roster, attendance records, and QR settings to the cloud
     * so all other connected student mobile devices stay in sync.
     */
    suspend fun pushToCloud(
        students: List<StudentEntity>,
        attendance: List<AttendanceEntity>,
        settings: LibrarySettingsEntity
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            val root = JSONObject()
            root.put("name", "RS_LIBRARY_DATA")

            val data = JSONObject()
            data.put("lastUpdated", System.currentTimeMillis())
            data.put("officialQrToken", settings.officialQrToken)
            data.put("isQrActive", settings.isQrActive)

            // Students array
            val studentsArr = JSONArray()
            students.forEach { s ->
                val obj = JSONObject().apply {
                    put("id", s.id)
                    put("username", s.username)
                    put("passwordHash", s.passwordHash)
                    put("fullName", s.fullName)
                    put("parentName", s.parentName)
                    put("mobileNumber", s.mobileNumber)
                    put("email", s.email)
                    put("address", s.address)
                    put("joiningDate", s.joiningDate)
                    put("seatNumber", s.seatNumber)
                    put("course", s.course)
                    put("status", s.status)
                    put("studentQrToken", s.studentQrToken)
                    put("createdAt", s.createdAt)
                }
                studentsArr.put(obj)
            }
            data.put("students", studentsArr)

            // Attendance array
            val attArr = JSONArray()
            attendance.forEach { a ->
                val obj = JSONObject().apply {
                    put("id", a.id)
                    put("studentId", a.studentId)
                    put("studentName", a.studentName)
                    put("date", a.date)
                    put("checkInTime", a.checkInTime)
                    if (a.checkOutTime != null) put("checkOutTime", a.checkOutTime) else put("checkOutTime", JSONObject.NULL)
                    put("status", a.status)
                    put("method", a.method)
                    put("remarks", a.remarks)
                    put("timestamp", a.timestamp)
                }
                attArr.put(obj)
            }
            data.put("attendance", attArr)

            root.put("data", data)

            val body = root.toString().toRequestBody(JSON_MEDIA_TYPE)
            val request = Request.Builder()
                .url(API_BASE_URL)
                .put(body)
                .build()

            val response = client.newCall(request).execute()
            val success = response.isSuccessful
            if (success) {
                Log.i(TAG, "Push completed successfully (${students.size} students, ${attendance.size} attendance)")
            } else {
                Log.w(TAG, "Push failed with HTTP code ${response.code}")
            }
            success
        } catch (e: Exception) {
            Log.e(TAG, "Push error: ${e.localizedMessage}")
            false
        }
    }
}
