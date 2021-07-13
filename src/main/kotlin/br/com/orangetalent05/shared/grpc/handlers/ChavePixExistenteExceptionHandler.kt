package br.com.orangetalent05.shared.grpc.handlers

import br.com.orangetalent05.pix.exceptions.ChavePixExistenteException
import br.com.orangetalent05.shared.grpc.ExceptionHandler
import br.com.orangetalent05.shared.grpc.ExceptionHandler.StatusWithDetails
import io.grpc.Status
import javax.inject.Singleton

@Singleton
class ChavePixExistenteExceptionHandler : ExceptionHandler<ChavePixExistenteException> {

    override fun handle(e: ChavePixExistenteException): StatusWithDetails {
        return StatusWithDetails(Status.ALREADY_EXISTS
            .withDescription(e.message)
            .withCause(e))
    }

    override fun supports(e: Exception): Boolean {
        return e is ChavePixExistenteException
    }

}