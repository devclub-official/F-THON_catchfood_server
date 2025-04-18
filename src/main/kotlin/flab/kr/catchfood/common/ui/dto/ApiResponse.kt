package flab.kr.catchfood.common.ui.dto

enum class ApiStatus {
    SUCCESS, FAIL
}

data class ApiResponse<T>(
    val status: ApiStatus,
    val data: T?,
    val message: String?
) {
    companion object {
        fun <T> success(data: T? = null): ApiResponse<T> {
            return ApiResponse(ApiStatus.SUCCESS, data, null)
        }

        fun <T> fail(message: String): ApiResponse<T> {
            return ApiResponse(ApiStatus.FAIL, null, message)
        }
    }
}
