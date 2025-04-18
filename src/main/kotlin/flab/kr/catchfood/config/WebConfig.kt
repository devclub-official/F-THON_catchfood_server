package flab.kr.catchfood.config

import flab.kr.catchfood.user.ui.resolver.CurrentUserArgumentResolver
import jakarta.servlet.Filter
import org.springframework.boot.web.servlet.FilterRegistrationBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

/**
 * Web configuration for the application.
 * Registers custom argument resolvers and filters.
 */
@Configuration
class WebConfig(
    private val currentUserArgumentResolver: CurrentUserArgumentResolver,
    private val requestResponseLoggingFilter: RequestResponseLoggingFilter
) : WebMvcConfigurer {

    override fun addArgumentResolvers(resolvers: MutableList<HandlerMethodArgumentResolver>) {
        resolvers.add(currentUserArgumentResolver)
    }

    /**
     * Registers the request/response logging filter with a high precedence to ensure
     * it's executed early in the filter chain.
     */
    @Bean
    fun loggingFilterRegistration(): FilterRegistrationBean<Filter> {
        val registrationBean = FilterRegistrationBean<Filter>()
        registrationBean.filter = requestResponseLoggingFilter
        registrationBean.addUrlPatterns("/*")
        registrationBean.order = 1
        return registrationBean
    }
}
