package com.example.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.PaymentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PaymentDao {
    @Query("SELECT * FROM payments ORDER BY createdAt DESC")
    fun getAllPayments(): Flow<List<PaymentEntity>>

    @Query("SELECT * FROM payments ORDER BY createdAt DESC")
    suspend fun getAllPaymentsList(): List<PaymentEntity>

    @Query("SELECT * FROM payments WHERE studentId = :studentId ORDER BY createdAt DESC")
    fun getPaymentsByStudent(studentId: String): Flow<List<PaymentEntity>>

    @Query("SELECT * FROM payments WHERE id = :id LIMIT 1")
    suspend fun getPaymentById(id: String): PaymentEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPayment(payment: PaymentEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllPayments(payments: List<PaymentEntity>)

    @Update
    suspend fun updatePayment(payment: PaymentEntity): Int

    @Query("UPDATE payments SET status = :status, adminNote = :adminNote, verifiedAt = :verifiedAt WHERE id = :id")
    suspend fun updateStatus(id: String, status: String, adminNote: String, verifiedAt: String): Int

    @Query("DELETE FROM payments WHERE id = :id")
    suspend fun deletePaymentById(id: String): Int
}
