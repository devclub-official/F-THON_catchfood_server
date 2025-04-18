package flab.kr.catchfood.common.exception

/**
 * Exception thrown when a user is not authorized to access a resource.
 * This will be translated to a 401 Unauthorized response.
 */
class UnauthorizedException(message: String) : RuntimeException(message)
