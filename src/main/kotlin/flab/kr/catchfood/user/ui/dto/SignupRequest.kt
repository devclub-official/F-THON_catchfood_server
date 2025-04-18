package flab.kr.catchfood.user.ui.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class SignupRequest(
    @field:NotBlank(message = "Name is required")
    @field:Size(max = 20, message = "Name must be less than 20 characters")
    val name: String,
)
