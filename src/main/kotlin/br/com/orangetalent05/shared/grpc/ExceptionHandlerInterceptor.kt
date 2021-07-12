package br.com.orangetalent05.shared.grpc

import io.grpc.BindableService
import io.grpc.stub.StreamObserver
import io.micronaut.aop.InterceptorBean
import io.micronaut.aop.MethodInterceptor
import io.micronaut.aop.MethodInvocationContext
import org.slf4j.LoggerFactory
import javax.inject.Inject
import javax.inject.Singleton

@InterceptorBean(ErrorHandler::class)
class ExceptionHandlerInterceptor(@Inject private val resolver: ExceptionHandlerResolver): MethodInterceptor<BindableService, Any?> {

    private val LOGGER = LoggerFactory.getLogger(this::class.java)

    override fun intercept(context: MethodInvocationContext<BindableService, Any?>): Any? {

        try {
            return context.proceed()
        } catch (e: Exception) {

            LOGGER.error("Handler the exception '${e.javaClass.name}' while processing the call: ${context.targetMethod}", e)

            @Suppress("UNCHECKED_CAST")
            val handler = resolver.resolve(e) as ExceptionHandler<Exception>
            val status = handler.handle(e)

            GrpcEndpointArguments(context).response()
                .onError(status.asRuntimeException())

            return null
        }

    }

    private class GrpcEndpointArguments(val context: MethodInvocationContext<BindableService, Any?>) {

        fun response(): StreamObserver<*> {
            return context.parameterValues[1] as StreamObserver<*>
        }
    }
}
