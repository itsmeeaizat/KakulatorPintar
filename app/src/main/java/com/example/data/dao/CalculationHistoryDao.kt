package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.entity.CalculationHistoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CalculationHistoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCalculation(calculation: CalculationHistoryEntity): Long

    @Query("SELECT * FROM calculation_history WHERE user_id = :userId ORDER BY created_at DESC")
    fun getHistoryByUserId(userId: Long): Flow<List<CalculationHistoryEntity>>

    @Query("SELECT * FROM calculation_history ORDER BY created_at DESC")
    fun getAllHistory(): Flow<List<CalculationHistoryEntity>>

    @Query("DELETE FROM calculation_history WHERE user_id = :userId")
    suspend fun clearHistoryByUserId(userId: Long)

    @Query("DELETE FROM calculation_history WHERE id = :id")
    suspend fun deleteHistoryById(id: Long)
}
