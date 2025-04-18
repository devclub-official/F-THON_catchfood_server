package flab.kr.catchfood.config

import flab.kr.catchfood.user.ui.resolver.CurrentUserArgumentResolver
import org.springframework.context.annotation.Configuration
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

/**
 * Web configuration for the application.
 * Registers custom argument resolvers.
 */
@Configuration
class WebConfig(private val currentUserArgumentResolver: CurrentUserArgumentResolver) : WebMvcConfigurer {

    override fun addArgumentResolvers(resolvers: MutableList<HandlerMethodArgumentResolver>) {
        resolvers.add(currentUserArgumentResolver)
    }
}
