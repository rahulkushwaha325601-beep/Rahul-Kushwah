package com.example.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.LibrarySettingsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LibrarySettingsDao {

    @Query("SELECT * FROM library_settings WHERE id = 1 LIMIT 1")
    fun getSettingsFlow(): Flow<LibrarySettingsEntity?>

    @Query("SELECT * FROM library_settings WHERE id = 1 LIMIT 1")
    suspend fun getSettings(): LibrarySettingsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(settings: LibrarySettingsEntity)

    @Update
    suspend fun update(settings: LibrarySettingsEntity)

    @Query("UPDATE library_settings SET officialQrToken = :newToken, qrGeneratedDate = :date WHERE id = 1")
    suspend fun updateQrToken(newToken: String, date: String)

    @Query("UPDATE library_settings SET isQrActive = :isActive WHERE id = 1")
    suspend fun setQrActive(isActive: Boolean)
}
