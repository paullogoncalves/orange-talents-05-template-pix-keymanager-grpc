package br.com.orangetalent05.shared.grpc.handlers

import br.com.orangetalent05.pix.exceptions.ChavePixExistenteException
import br.com.orangetalent05.pix.exceptions.ChavePixNaoEncontradaException
import br.com.orangetalent05.shared.grpc.ExceptionHandler
import io.grpc.Status
import javax.inject.Singleton

@Singleton
class ChavePixNaoEncontradaExceptionHandle : ExceptionHandler<ChavePixNaoEncontradaException> {

    override fun handle(e: ChavePixNaoEncontradaException): ExceptionHandler.StatusWithDetails {
        return ExceptionHandler.StatusWithDetails(Status.NOT_FOUND
            .withDescription(e.message)
            .withCause(e))
    }

    override fun supports(e: Exception): Boolean {
        return e is ChavePixNaoEncontradaException
    }

}