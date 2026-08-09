package com.example.api.controller

import com.example.api.model.ApiResponse
import com.example.api.model.CalculateRequest
import com.example.api.model.CalculateResponseData
import com.example.api.model.DeleteResponseData
import com.example.api.model.HistoryItemResponse
import com.example.data.dao.CalculationHistoryDao
import com.example.data.dao.UserDao
import com.example.data.entity.CalculationHistoryEntity
import com.example.domain.CalculationResult
import com.example.domain.CalculatorEngine
import kotlinx.coroutines.flow.first
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Controller Architecture implementing REST API Endpoints for Calculator & History.
 */
class CalculatorApiController(
    private val calculationHistoryDao: CalculationHistoryDao,
    private val userDao: UserDao
) {
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

    /**
     * POST /api/calculate
     * Menyimpan hasil perhitungan baru ke database setelah validasi backend
     */
    suspend fun postCalculate(request: CalculateRequest): ApiResponse<CalculateResponseData> {
        val userId = request.userId ?: 1L

        // Validate user existence or fallback to default
        val user = userDao.getUserById(userId) ?: userDao.getDefaultUser()
        val targetUserId = user?.id ?: 1L

        // Business Logic & Validation Engine
        val result = CalculatorEngine.calculate(
            firstOperandStr = request.firstOperand,
            operator = request.operator,
            secondOperandStr = request.secondOperand
        )

        return when (result) {
            is CalculationResult.Success -> {
                val timestamp = System.currentTimeMillis()
                val entity = CalculationHistoryEntity(
                    userId = targetUserId,
                    firstOperand = result.firstOperand,
                    operator = result.operator,
                    secondOperand = result.secondOperand,
                    expression = result.expression,
                    result = result.result,
                    createdAt = timestamp
                )

                val newId = calculationHistoryDao.insertCalculation(entity)

                ApiResponse(
                    status = "success",
                    code = 201,
                    message = "Kalkulasi berhasil diproses dan disimpan ke riwayat.",
                    data = CalculateResponseData(
                        id = newId,
                        userId = targetUserId,
                        firstOperand = result.firstOperand,
                        operator = result.operator,
                        secondOperand = result.secondOperand,
                        expression = result.expression,
                        result = result.result,
                        createdAt = timestamp
                    )
                )
            }
            is CalculationResult.Error -> {
                ApiResponse(
                    status = "error",
                    code = 400,
                    message = result.errorMessage,
                    data = null
                )
            }
        }
    }

    /**
     * GET /api/history?user_id=1
     * Mengambil daftar riwayat perhitungan user dari database
     */
    suspend fun getHistory(userId: Long = 1L): ApiResponse<List<HistoryItemResponse>> {
        return try {
            val historyEntities = calculationHistoryDao.getHistoryByUserId(userId).first()
            val responseItems = historyEntities.map { entity ->
                HistoryItemResponse(
                    id = entity.id,
                    userId = entity.userId,
                    firstOperand = entity.firstOperand,
                    operator = entity.operator,
                    secondOperand = entity.secondOperand,
                    expression = entity.expression,
                    result = entity.result,
                    createdAt = entity.createdAt,
                    createdAtFormatted = dateFormat.format(Date(entity.createdAt))
                )
            }

            ApiResponse(
                status = "success",
                code = 200,
                message = "Berhasil mengambil ${responseItems.size} data riwayat.",
                data = responseItems
            )
        } catch (e: Exception) {
            ApiResponse(
                status = "error",
                code = 500,
                message = "Gagal mengambil data riwayat: ${e.localizedMessage}",
                data = null
            )
        }
    }

    /**
     * DELETE /api/history/:id
     * Menghapus item riwayat perhitungan berdasarkan ID
     */
    suspend fun deleteHistoryById(id: Long): ApiResponse<DeleteResponseData> {
        return try {
            calculationHistoryDao.deleteHistoryById(id)
            ApiResponse(
                status = "success",
                code = 200,
                message = "Riwayat dengan ID $id berhasil dihapus.",
                data = DeleteResponseData(deletedId = id)
            )
        } catch (e: Exception) {
            ApiResponse(
                status = "error",
                code = 500,
                message = "Gagal menghapus riwayat ID $id: ${e.localizedMessage}",
                data = null
            )
        }
    }

    /**
     * DELETE /api/history?user_id=1
     * Menghapus seluruh riwayat perhitungan user (Bulk Clear)
     */
    suspend fun deleteAllHistory(userId: Long = 1L): ApiResponse<DeleteResponseData> {
        return try {
            calculationHistoryDao.clearHistoryByUserId(userId)
            ApiResponse(
                status = "success",
                code = 200,
                message = "Seluruh riwayat perhitungan berhasil dibersihkan.",
                data = DeleteResponseData(totalDeleted = -1)
            )
        } catch (e: Exception) {
            ApiResponse(
                status = "error",
                code = 500,
                message = "Gagal menghapus seluruh riwayat: ${e.localizedMessage}",
                data = null
            )
        }
    }
}
