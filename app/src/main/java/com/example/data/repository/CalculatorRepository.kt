package com.example.data.repository

import com.example.data.dao.CalculationHistoryDao
import com.example.data.dao.UserDao
import com.example.data.entity.CalculationHistoryEntity
import com.example.data.entity.UserEntity
import com.example.domain.CalculationResult
import com.example.domain.CalculatorEngine
import kotlinx.coroutines.flow.Flow

class CalculatorRepository(
    private val calculationHistoryDao: CalculationHistoryDao,
    private val userDao: UserDao
) {
    fun getHistoryForUser(userId: Long): Flow<List<CalculationHistoryEntity>> {
        return calculationHistoryDao.getHistoryByUserId(userId)
    }

    suspend fun ensureDefaultUser(): UserEntity {
        val existingUser = userDao.getDefaultUser()
        if (existingUser != null) {
            return existingUser
        }
        val defaultUser = UserEntity(username = "Pengguna Utama", email = "user@kalkulator.local")
        val newId = userDao.insertUser(defaultUser)
        return defaultUser.copy(id = newId)
    }

    suspend fun processAndSaveCalculation(
        userId: Long,
        firstOperand: String,
        operator: String,
        secondOperand: String
    ): CalculationResult {
        val result = CalculatorEngine.calculate(firstOperand, operator, secondOperand)
        if (result is CalculationResult.Success) {
            val entity = CalculationHistoryEntity(
                userId = userId,
                firstOperand = result.firstOperand,
                operator = result.operator,
                secondOperand = result.secondOperand,
                expression = result.expression,
                result = result.result
            )
            calculationHistoryDao.insertCalculation(entity)
        }
        return result
    }

    suspend fun clearHistory(userId: Long) {
        calculationHistoryDao.clearHistoryByUserId(userId)
    }

    suspend fun deleteHistoryItem(id: Long) {
        calculationHistoryDao.deleteHistoryById(id)
    }
}
