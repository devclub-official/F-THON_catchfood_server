package flab.kr.catchfood.common.ui

import flab.kr.catchfood.common.exception.UnauthorizedException
import flab.kr.catchfood.common.ui.dto.ApiResponse
import flab.kr.catchfood.config.RequestResponseLoggingFilter
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.validation.FieldError
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {

    companion object {
        private val log = LoggerFactory.getLogger(RequestResponseLoggingFilter::class.java)
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handleValidationExceptions(ex: MethodArgumentNotValidException): ApiResponse<Void> {
        val errors = ex.bindingResult.allErrors.joinToString(", ") { error ->
            val fieldName = (error as? FieldError)?.field ?: ""
            val message = error.defaultMessage ?: ""
            if (fieldName.isNotEmpty()) "$fieldName: $message" else message
        }
        return ApiResponse.fail(errors)
    }

    @ExceptionHandler(IllegalArgumentException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handleIllegalArgumentException(ex: IllegalArgumentException): ApiResponse<Void> {
        return ApiResponse.fail(ex.message ?: "Bad request")
    }

    @ExceptionHandler(UnauthorizedException::class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    fun handleUnauthorizedException(ex: UnauthorizedException): ApiResponse<Void> {
        return ApiResponse.fail(ex.message ?: "Unauthorized")
    }

    @ExceptionHandler(Exception::class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    fun handleAllExceptions(ex: Exception): ApiResponse<Void> {
        log.error("An unexpected error occurred: ${ex.message}", ex)
        return ApiResponse.fail("An unexpected error occurred: ${ex.message}")
    }
}
