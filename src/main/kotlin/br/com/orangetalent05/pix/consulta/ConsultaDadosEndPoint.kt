package br.com.orangetalent05.pix.consulta

import br.com.orangetalent05.ConsultaChavePixRequest
import br.com.orangetalent05.ConsultaChavePixResponse
import br.com.orangetalent05.PixKeymanagerConsultaGrpcServiceGrpc
import br.com.orangetalent05.integration.bcb.BancoCentralClient
import br.com.orangetalent05.pix.ChavePixRepository
import br.com.orangetalent05.shared.grpc.ErrorHandler
import io.grpc.stub.StreamObserver
import javax.inject.Inject
import javax.inject.Singleton
import javax.validation.Validator

@ErrorHandler
@Singleton
class ConsultaDadosEndPoint(
    @Inject private val chaveRepo: ChavePixRepository,
    @Inject private val validator: Validator,
    @Inject private val bancoCentralClient: BancoCentralClient
):
    PixKeymanagerConsultaGrpcServiceGrpc.PixKeymanagerConsultaGrpcServiceImplBase() {

    override fun busca(
        request: ConsultaChavePixRequest,
        responseObserver: StreamObserver<ConsultaChavePixResponse>?,
    ) {

        val filtro = request.toModel(validator)
        val chavePixInfo = filtro.filtra(chaveRepo, bancoCentralClient)

        responseObserver?.onNext(ConsultaChavePixConverter().convert(chavePixInfo))
        responseObserver?.onCompleted()


    }
}