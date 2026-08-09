package com.example.api.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Standard API Wrapper Response for all REST endpoints.
 */
@JsonClass(generateAdapter = true)
data class ApiResponse<T>(
    @Json(name = "status") val status: String, // "success" or "error"
    @Json(name = "code") val code: Int,
    @Json(name = "message") val message: String,
    @Json(name = "data") val data: T? = null
)

/**
 * Request payload for POST /api/calculate
 */
@JsonClass(generateAdapter = true)
data class CalculateRequest(
    @Json(name = "user_id") val userId: Long? = 1L,
    @Json(name = "first_operand") val firstOperand: String,
    @Json(name = "operator") val operator: String,
    @Json(name = "second_operand") val secondOperand: String
)

/**
 * Response payload data for POST /api/calculate
 */
@JsonClass(generateAdapter = true)
data class CalculateResponseData(
    @Json(name = "id") val id: Long? = null,
    @Json(name = "user_id") val userId: Long,
    @Json(name = "first_operand") val firstOperand: String,
    @Json(name = "operator") val operator: String,
    @Json(name = "second_operand") val secondOperand: String,
    @Json(name = "expression") val expression: String,
    @Json(name = "result") val result: String,
    @Json(name = "created_at") val createdAt: Long
)

/**
 * Item element for GET /api/history list
 */
@JsonClass(generateAdapter = true)
data class HistoryItemResponse(
    @Json(name = "id") val id: Long,
    @Json(name = "user_id") val userId: Long,
    @Json(name = "first_operand") val firstOperand: String,
    @Json(name = "operator") val operator: String,
    @Json(name = "second_operand") val secondOperand: String,
    @Json(name = "expression") val expression: String,
    @Json(name = "result") val result: String,
    @Json(name = "created_at") val createdAt: Long,
    @Json(name = "created_at_formatted") val createdAtFormatted: String
)

/**
 * Response payload data for DELETE endpoints
 */
@JsonClass(generateAdapter = true)
data class DeleteResponseData(
    @Json(name = "deleted_id") val deletedId: Long? = null,
    @Json(name = "total_deleted") val totalDeleted: Int? = null
)
