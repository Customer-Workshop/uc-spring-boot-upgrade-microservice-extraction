package com.bank.rm.common.exception

import com.bank.rm.common.dto.ApiResponse
import com.bank.rm.common.dto.ErrorDetail
import com.bank.rm.common.dto.FieldError
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {

    private val log = LoggerFactory.getLogger(javaClass)

    @ExceptionHandler(ResourceNotFoundException::class)
    fun handleNotFound(ex: ResourceNotFoundException): ResponseEntity<ApiResponse<Nothing>> =
        ResponseEntity.status(HttpStatus.NOT_FOUND).body(
            ApiResponse(success = false, error = ErrorDetail("NOT_FOUND", ex.message ?: "Resource not found"))
        )

    @ExceptionHandler(ValidationException::class)
    fun handleValidation(ex: ValidationException): ResponseEntity<ApiResponse<Nothing>> =
        ResponseEntity.badRequest().body(
            ApiResponse(success = false, error = ErrorDetail("VALIDATION_ERROR", ex.message ?: "Validation failed"))
        )

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleMethodArgumentNotValid(ex: MethodArgumentNotValidException): ResponseEntity<ApiResponse<Nothing>> {
        val fieldErrors = ex.bindingResult.fieldErrors.map {
            FieldError(it.field, it.defaultMessage ?: "Invalid value", it.rejectedValue)
        }
        return ResponseEntity.badRequest().body(
            ApiResponse(success = false, error = ErrorDetail("VALIDATION_ERROR", "Invalid input data", fieldErrors))
        )
    }

    @ExceptionHandler(AIServiceException::class)
    fun handleAIService(ex: AIServiceException): ResponseEntity<ApiResponse<Nothing>> {
        log.error("AI service error: ${ex.message}", ex)
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(
            ApiResponse(success = false, error = ErrorDetail("AI_SERVICE_ERROR", ex.message ?: "AI service unavailable"))
        )
    }

    @ExceptionHandler(Exception::class)
    fun handleGeneral(ex: Exception): ResponseEntity<ApiResponse<Nothing>> {
        log.error("Unhandled exception: ${ex.message}", ex)
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
            ApiResponse(success = false, error = ErrorDetail("INTERNAL_ERROR", "An unexpected error occurred"))
        )
    }
}
