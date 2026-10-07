package com.example.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.StudentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface StudentDao {

    @Query("SELECT * FROM students ORDER BY createdAt DESC")
    fun getAllStudents(): Flow<List<StudentEntity>>

    @Query("SELECT * FROM students ORDER BY createdAt DESC")
    suspend fun getAllStudentsList(): List<StudentEntity>

    @Query("SELECT * FROM students WHERE id = :id LIMIT 1")
    suspend fun getStudentById(id: String): StudentEntity?

    @Query("SELECT * FROM students WHERE username = :username LIMIT 1")
    suspend fun getStudentByUsername(username: String): StudentEntity?

    @Query("SELECT * FROM students WHERE mobileNumber = :mobile LIMIT 1")
    suspend fun getStudentByMobile(mobile: String): StudentEntity?

    @Query("SELECT * FROM students WHERE id = :identifier OR username = :identifier OR mobileNumber = :identifier LIMIT 1")
    suspend fun findStudentByIdentifier(identifier: String): StudentEntity?

    @Query("SELECT * FROM students WHERE status = 'Active'")
    fun getActiveStudents(): Flow<List<StudentEntity>>

    @Query("SELECT COUNT(*) FROM students")
    fun getTotalStudentsCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM students WHERE status = 'Active'")
    fun getActiveStudentsCount(): Flow<Int>

    @Query("""
        SELECT * FROM students 
        WHERE fullName LIKE '%' || :query || '%' 
           OR id LIKE '%' || :query || '%' 
           OR username LIKE '%' || :query || '%'
           OR mobileNumber LIKE '%' || :query || '%' 
           OR seatNumber LIKE '%' || :query || '%'
        ORDER BY fullName ASC
    """)
    fun searchStudents(query: String): Flow<List<StudentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudent(student: StudentEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllStudents(students: List<StudentEntity>)

    @Update
    suspend fun updateStudent(student: StudentEntity)

    @Delete
    suspend fun deleteStudent(student: StudentEntity)

    @Query("DELETE FROM students WHERE id = :studentId")
    suspend fun deleteStudentById(studentId: String)

    @Query("UPDATE students SET passwordHash = :newHash WHERE id = :studentId")
    suspend fun updateStudentPassword(studentId: String, newHash: String)

    @Query("UPDATE students SET status = :status WHERE id = :studentId")
    suspend fun updateStudentStatus(studentId: String, status: String)

    @Query("DELETE FROM students")
    suspend fun deleteAllStudents()

    @Query("SELECT COUNT(*) FROM students")
    suspend fun countStudents(): Int
}
