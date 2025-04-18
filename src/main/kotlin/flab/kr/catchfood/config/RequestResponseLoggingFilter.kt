package flab.kr.catchfood.config

import jakarta.servlet.Filter
import jakarta.servlet.FilterChain
import jakarta.servlet.ServletRequest
import jakarta.servlet.ServletResponse
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.web.util.ContentCachingRequestWrapper
import org.springframework.web.util.ContentCachingResponseWrapper
import java.nio.charset.StandardCharsets

/**
 * Filter that logs all request and response headers and bodies.
 * Uses ContentCachingRequestWrapper and ContentCachingResponseWrapper to cache the request and response bodies
 * so they can be read multiple times.
 * 
 * Note: Using SLF4J for logging as requested in the issue description.
 */
@Component
class RequestResponseLoggingFilter : Filter {

    companion object {
        private val log = LoggerFactory.getLogger(RequestResponseLoggingFilter::class.java)
    }

    override fun doFilter(request: ServletRequest, response: ServletResponse, chain: FilterChain) {
        val httpRequest = request as HttpServletRequest
        val httpResponse = response as HttpServletResponse

        val requestWrapper = ContentCachingRequestWrapper(httpRequest)
        val responseWrapper = ContentCachingResponseWrapper(httpResponse)

        val startTime = System.currentTimeMillis()

        // Log request details
        logRequest(requestWrapper)

        try {
            // Pass request through filter chain
            chain.doFilter(requestWrapper, responseWrapper)
        } finally {
            // Log response details
            logResponse(responseWrapper, System.currentTimeMillis() - startTime)

            // Copy content back to original response
            responseWrapper.copyBodyToResponse()
        }
    }

    private fun logRequest(request: ContentCachingRequestWrapper) {
        val uri = request.requestURI
        val method = request.method

        log.info("=== REQUEST ===")
        log.info("URI: $uri")
        log.info("Method: $method")
        log.info("Headers: {}", getHeadersAsString(request))

        // Log request body
        val content = request.contentAsByteArray
        if (content.isNotEmpty()) {
            log.info("Request Body: {}", String(content, StandardCharsets.UTF_8))
        }
    }

    private fun logResponse(response: ContentCachingResponseWrapper, timeElapsed: Long) {
        val status = response.status

        log.info("=== RESPONSE ===")
        log.info("Status: $status")
        log.info("Time Elapsed: $timeElapsed ms")
        log.info("Headers: {}", getResponseHeadersAsString(response))

        // Log response body
        val content = response.contentAsByteArray
        if (content.isNotEmpty()) {
            log.info("Response Body: {}", String(content, StandardCharsets.UTF_8))
        }
    }

    private fun getHeadersAsString(request: HttpServletRequest): String {
        val headers = StringBuilder()
        val headerNames = request.headerNames

        while (headerNames.hasMoreElements()) {
            val headerName = headerNames.nextElement()
            headers.append("$headerName: ${request.getHeader(headerName)}, ")
        }

        return headers.toString()
    }

    private fun getResponseHeadersAsString(response: HttpServletResponse): String {
        val headers = StringBuilder()
        val headerNames = response.headerNames

        for (headerName in headerNames) {
            headers.append("$headerName: ${response.getHeader(headerName)}, ")
        }

        return headers.toString()
    }
}
