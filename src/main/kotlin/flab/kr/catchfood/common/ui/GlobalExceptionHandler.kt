package flab.kr.catchfood.common.ui

import flab.kr.catchfood.common.ui.dto.ApiResponse
import org.springframework.http.HttpStatus
import org.springframework.validation.FieldError
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handleValidationExceptions(ex: MethodArgumentNotValidException): ApiResponse<Any> {
        val errors = ex.bindingResult.allErrors.joinToString(", ") { error ->
            val fieldName = (error as? FieldError)?.field ?: ""
            val message = error.defaultMessage ?: ""
            if (fieldName.isNotEmpty()) "$fieldName: $message" else message
        }
        return ApiResponse.fail(errors)
    }

    @ExceptionHandler(IllegalArgumentException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handleIllegalArgumentException(ex: IllegalArgumentException): ApiResponse<Any> {
        return ApiResponse.fail(ex.message ?: "Bad request")
    }

    @ExceptionHandler(Exception::class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    fun handleAllExceptions(ex: Exception): ApiResponse<Any> {
        return ApiResponse.fail("An unexpected error occurred: ${ex.message}")
    }
}
