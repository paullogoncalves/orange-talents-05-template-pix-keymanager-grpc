package br.com.orangetalent05.pix.listagem

import br.com.orangetalent05.*
import br.com.orangetalent05.pix.ChavePixRepository
import br.com.orangetalent05.shared.grpc.ErrorHandler
import com.google.protobuf.Timestamp
import io.grpc.stub.StreamObserver
import java.lang.IllegalArgumentException
import java.time.ZoneId
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@ErrorHandler
@Singleton
class ListaChavesEndPoint(@Inject private val repository: ChavePixRepository
): PixKeymanagerListaGrpcServiceGrpc.PixKeymanagerListaGrpcServiceImplBase() {

    override fun lista(request: ListaChavesPixRequest, responseObserver: StreamObserver<ListaChavesPixResponse>) {

        if (request.clientId.isNullOrBlank())
            throw IllegalArgumentException("Cliente ID não pode ser nulo ou vazio")

        val clienteId = UUID.fromString(request.clientId)
        val chaves = repository.findAllByClientId(clienteId).map {
            ListaChavesPixResponse.ChavePix.newBuilder()
                .setPixId(it.id.toString())
                .setTipo(TipoDeChave.valueOf(it.tipo.name))
                .setChave(it.chave)
                .setTipoDeConta(TipoDeConta.valueOf(it.tipoDeConta.name))
                .setCriadaEm(it.criadoEm.let {
                    val createdAt = it.atZone(ZoneId.of("UTC")).toInstant()
                    Timestamp.newBuilder()
                        .setSeconds(createdAt.epochSecond)
                        .setNanos(createdAt.nano)
                        .build()
                })
                .build()
        }

        responseObserver.onNext(ListaChavesPixResponse.newBuilder()
            .setClienteId(clienteId.toString())
            .addAllChaves(chaves)
            .build())
        responseObserver.onCompleted()
    }

}
