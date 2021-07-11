package br.com.orangetalent05.pix.registra

import br.com.orangetalent05.PixKeymanagerGrpcServiceGrpc
import br.com.orangetalent05.RegistraChavePixRequest
import br.com.orangetalent05.RegistraChavePixResponse
import br.com.orangetalent05.integration.itau.ItauClient
import io.grpc.stub.StreamObserver
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RegistraChaveEndPoint(@Inject private val service: NovaChavePixService): PixKeymanagerGrpcServiceGrpc.PixKeymanagerGrpcServiceImplBase() {

    override fun registra(
        request: RegistraChavePixRequest?,
        responseObserver: StreamObserver<RegistraChavePixResponse>?
    ) {
        val novaChave = request?.toModel()
        val chaveCriada = service.registra(novaChave!!)

        responseObserver?.onNext(RegistraChavePixResponse.newBuilder()
            .setClienteId(chaveCriada.clientId.toString())
            .setPixId(chaveCriada.id.toString())
            .build())

        responseObserver?.onCompleted()




    }

}