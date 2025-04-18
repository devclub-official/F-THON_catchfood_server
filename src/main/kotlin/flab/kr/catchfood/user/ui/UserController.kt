package flab.kr.catchfood.user.ui

import flab.kr.catchfood.common.ui.dto.ApiResponse
import flab.kr.catchfood.user.application.UserService
import flab.kr.catchfood.user.ui.dto.SignupRequest
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@RestController
class UserController(private val userService: UserService) {

    @PostMapping("/signup")
    @ResponseStatus(HttpStatus.CREATED)
    fun signup(@Valid @RequestBody request: SignupRequest): ApiResponse<Any> {
        userService.signup(request)
        return ApiResponse.success()
    }

    @GetMapping("/members")
    fun getAllMembers(): ApiResponse<List<Map<String, String>>> {
        val users = userService.getAllUsers()
        val response = users.map { mapOf("name" to it.name) }
        return ApiResponse.success(response)
    }
}
