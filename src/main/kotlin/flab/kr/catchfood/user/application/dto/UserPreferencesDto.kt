package flab.kr.catchfood.user.application.dto

/**
 * DTO for user preferences
 */
data class UserPreferencesDto(
    val likes: String? = null,
    val dislikes: String? = null,
    val etc: String? = null
)
