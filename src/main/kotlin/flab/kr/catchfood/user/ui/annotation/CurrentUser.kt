package flab.kr.catchfood.user.ui.annotation

import kotlin.annotation.AnnotationRetention
import kotlin.annotation.AnnotationTarget
import kotlin.annotation.Retention
import kotlin.annotation.Target

/**
 * Annotation to inject the current user into controller methods.
 * The current user is determined from the X-User-Name header.
 */
@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
annotation class CurrentUser
