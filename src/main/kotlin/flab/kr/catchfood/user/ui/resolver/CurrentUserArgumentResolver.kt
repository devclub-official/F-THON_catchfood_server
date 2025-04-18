package flab.kr.catchfood.user.ui.resolver

import flab.kr.catchfood.common.exception.UnauthorizedException
import flab.kr.catchfood.user.application.UserService
import flab.kr.catchfood.user.domain.User
import flab.kr.catchfood.user.ui.annotation.CurrentUser
import org.springframework.core.MethodParameter
import org.springframework.stereotype.Component
import org.springframework.web.bind.support.WebDataBinderFactory
import org.springframework.web.context.request.NativeWebRequest
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.method.support.ModelAndViewContainer

/**
 * Resolver for the @CurrentUser annotation.
 * Extracts the username from the X-User-Name header and resolves it to a User object.
 */
@Component
class CurrentUserArgumentResolver(
    private val userService: UserService
) : HandlerMethodArgumentResolver {

    override fun supportsParameter(parameter: MethodParameter): Boolean {
        return parameter.hasParameterAnnotation(CurrentUser::class.java) && 
               parameter.parameterType == User::class.java
    }

    override fun resolveArgument(
        parameter: MethodParameter,
        mavContainer: ModelAndViewContainer?,
        webRequest: NativeWebRequest,
        binderFactory: WebDataBinderFactory?
    ): Any? {
        // Check for X-User-Name header
        val userName = webRequest.getHeader("X-User-Name") 
            ?: throw UnauthorizedException("X-User-Name header is required")

        try {
            // Get user from service
            return userService.getUserByName(userName)
        } catch (e: IllegalArgumentException) {
            // If user not found, throw UnauthorizedException
            throw UnauthorizedException("User not found: ${e.message}")
        }
    }
}
