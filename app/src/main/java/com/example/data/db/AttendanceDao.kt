package com.example.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.AttendanceEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AttendanceDao {

    @Query("SELECT * FROM attendance ORDER BY timestamp DESC")
    fun getAllAttendance(): Flow<List<AttendanceEntity>>

    @Query("SELECT * FROM attendance ORDER BY timestamp DESC")
    suspend fun getAllAttendanceList(): List<AttendanceEntity>

    @Query("SELECT * FROM attendance WHERE date = :date ORDER BY timestamp DESC")
    fun getAttendanceByDate(date: String): Flow<List<AttendanceEntity>>

    @Query("SELECT * FROM attendance WHERE studentId = :studentId ORDER BY date DESC, timestamp DESC")
    fun getAttendanceForStudent(studentId: String): Flow<List<AttendanceEntity>>

    @Query("SELECT * FROM attendance WHERE studentId = :studentId AND date = :date LIMIT 1")
    suspend fun getTodayAttendanceForStudent(studentId: String, date: String): AttendanceEntity?

    @Query("SELECT COUNT(*) FROM attendance WHERE date = :date AND status = 'Present'")
    fun getPresentCountForDate(date: String): Flow<Int>

    @Query("SELECT COUNT(*) FROM attendance")
    fun getTotalAttendanceCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM attendance WHERE studentId = :studentId AND status = 'Present'")
    fun getStudentPresentCount(studentId: String): Flow<Int>

    @Query("SELECT * FROM attendance WHERE date LIKE :monthPrefix || '%' ORDER BY date DESC")
    fun getAttendanceByMonth(monthPrefix: String): Flow<List<AttendanceEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttendance(attendance: AttendanceEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllAttendance(list: List<AttendanceEntity>)

    @Update
    suspend fun updateAttendance(attendance: AttendanceEntity)

    @Delete
    suspend fun deleteAttendance(attendance: AttendanceEntity)

    @Query("DELETE FROM attendance WHERE id = :id")
    suspend fun deleteAttendanceById(id: Long)

    @Query("SELECT * FROM attendance WHERE id = :id LIMIT 1")
    suspend fun getAttendanceById(id: Long): AttendanceEntity?

    @Query("UPDATE attendance SET checkOutTime = :outTime WHERE id = :id")
    suspend fun markCheckOut(id: Long, outTime: String)

    @Query("UPDATE attendance SET status = :status WHERE id = :id")
    suspend fun updateAttendanceStatus(id: Long, status: String)

    @Query("DELETE FROM attendance WHERE studentId = :studentId")
    suspend fun deleteAttendanceForStudent(studentId: String)

    @Query("DELETE FROM attendance")
    suspend fun deleteAllAttendance()

    @Query("SELECT COUNT(*) FROM attendance")
    suspend fun countAttendance(): Int
}
