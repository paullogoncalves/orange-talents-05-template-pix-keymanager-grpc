package br.com.orangetalent05.pix.remove

import br.com.orangetalent05.PixKeymanagerGrpcServiceGrpc
import br.com.orangetalent05.PixKeymanagerRemoveGrpcServiceGrpc
import br.com.orangetalent05.RemoveChavePixRequest
import br.com.orangetalent05.RemoveChavePixResponse
import br.com.orangetalent05.shared.grpc.ErrorHandler
import io.grpc.stub.StreamObserver
import javax.inject.Inject
import javax.inject.Singleton

@ErrorHandler
@Singleton
class RemoveChaveEndPoint(@Inject private val service: RemoveChaveService ):
    PixKeymanagerRemoveGrpcServiceGrpc.PixKeymanagerRemoveGrpcServiceImplBase() {

    override fun remove(request: RemoveChavePixRequest,
                        responseObserver: StreamObserver<RemoveChavePixResponse>) {

        service.remove(clientId = request.clientId, pixId = request.pixId )

        responseObserver.onNext(RemoveChavePixResponse.newBuilder()
                        .setClientId(request.clientId)
                        .setPixId(request.pixId)
                        .build())
        responseObserver.onCompleted()
    }
}