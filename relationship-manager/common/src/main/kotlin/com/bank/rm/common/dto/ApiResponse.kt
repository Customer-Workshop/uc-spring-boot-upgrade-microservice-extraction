package com.bank.rm.common.dto

import java.time.Instant

data class ApiResponse<T>(
    val success: Boolean,
    val data: T? = null,
    val error: ErrorDetail? = null,
    val timestamp: Instant = Instant.now()
)

data class ErrorDetail(
    val code: String,
    val message: String,
    val details: List<FieldError>? = null,
    val traceId: String? = null
)

data class FieldError(
    val field: String,
    val message: String,
    val rejectedValue: Any? = null
)

data class PagedResponse<T>(
    val content: List<T>,
    val page: Int,
    val size: Int,
    val totalElements: Long,
    val totalPages: Int
)
