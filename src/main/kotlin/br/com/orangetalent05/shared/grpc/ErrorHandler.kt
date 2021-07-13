package br.com.orangetalent05.shared.grpc

import io.micronaut.aop.Around
import kotlin.annotation.AnnotationRetention.RUNTIME
import io.micronaut.context.annotation.Type
import kotlin.annotation.AnnotationTarget.*


@MustBeDocumented
@Retention(RUNTIME)
@Target(CLASS, FILE, FUNCTION, PROPERTY_GETTER, PROPERTY_SETTER)
@Around
@Type(ExceptionHandlerInterceptor::class)
annotation class ErrorHandler()
