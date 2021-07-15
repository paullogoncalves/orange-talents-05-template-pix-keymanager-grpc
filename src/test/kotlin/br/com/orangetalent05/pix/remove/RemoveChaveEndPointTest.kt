package br.com.orangetalent05.pix.remove

import br.com.orangetalent05.PixKeymanagerRemoveGrpcServiceGrpc
import br.com.orangetalent05.RemoveChavePixRequest
import br.com.orangetalent05.integration.bcb.BancoCentralClient
import br.com.orangetalent05.pix.*
import io.grpc.ManagedChannel
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.micronaut.context.annotation.Bean
import io.micronaut.context.annotation.Factory
import io.micronaut.grpc.annotation.GrpcChannel
import io.micronaut.grpc.server.GrpcServerChannel
import io.micronaut.test.annotation.MockBean
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito
import java.util.*

@MicronautTest(transactional = false)
internal class RemoveChaveEndPointTest(
    val repo: ChavePixRepository,
    val grpcClient: PixKeymanagerRemoveGrpcServiceGrpc.PixKeymanagerRemoveGrpcServiceBlockingStub
    ) {

    lateinit var bcbClient: BancoCentralClient

    lateinit var CHAVE_ATIVA: ChavePix

    @BeforeEach
    fun setUp() {
        CHAVE_ATIVA = repo.save(chave(
            tipo = TipoDeChave.EMAIL,
            chave = "Paulo@gmail.com",
            clientId = UUID.randomUUID()
        ))
    }

    @AfterEach
    fun cleanUp() {
        repo.deleteAll()
    }

    @Test
    fun `deve deletar chave pix do banco`() {

        val response = grpcClient.remove(RemoveChavePixRequest.newBuilder()
                                 .setPixId(CHAVE_ATIVA.id.toString())
                                 .setClientId(CHAVE_ATIVA.clientId.toString())
                                 .build())

        assertEquals(CHAVE_ATIVA.id.toString(), response.pixId)
        assertEquals(CHAVE_ATIVA.clientId.toString(), response.clientId)

    }


    @Test
    fun `nao deve deletar chave quando chave nao existir`() {

        val chaveInexistente = UUID.randomUUID().toString()

        val response = assertThrows<StatusRuntimeException> {
            grpcClient.remove(RemoveChavePixRequest.newBuilder()
                .setPixId(chaveInexistente)
                .setClientId(CHAVE_ATIVA.clientId.toString())
                .build())
        }

        with(response) {
            assertEquals(Status.NOT_FOUND.code, status.code)
            assertEquals("Chave não encontrada", status.description)
        }

    }

    @Test
    fun `nao deve deletar chave quando chave existir mas pertence a outro cliente`() {

        val clienteInexistente = UUID.randomUUID().toString()

        val response = assertThrows<StatusRuntimeException> {
            grpcClient.remove(RemoveChavePixRequest.newBuilder()
                .setPixId(CHAVE_ATIVA.id.toString())
                .setClientId(clienteInexistente)
                .build())
        }

        with(response) {
            assertEquals(Status.NOT_FOUND.code, status.code)
            assertEquals("Chave não encontrada", status.description)
        }

    }

    @MockBean(BancoCentralClient::class)
    fun bcbClient(): BancoCentralClient {
        return Mockito.mock(BancoCentralClient::class.java)
    }

    @Factory
    class Clients {
        @Bean
        fun blockingStub(@GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel): PixKeymanagerRemoveGrpcServiceGrpc.PixKeymanagerRemoveGrpcServiceBlockingStub? {
            return PixKeymanagerRemoveGrpcServiceGrpc.newBlockingStub(channel)
        }
    }

    private fun chave(
        tipo: TipoDeChave,
        chave: String = UUID.randomUUID().toString(),
        clientId: UUID = UUID.randomUUID(),
    ): ChavePix {
        return ChavePix(
            clientId = clientId,
            tipo = tipo,
            chave = chave,
            tipoDeConta = TipoDeConta.CONTA_CORRENTE,
            conta = ContaAssociada(
                instituicao = "UNIBANCO ITAU",
                nomeDoTitular = "Paulo Roberto",
                cpfDoTitular = "12345678900",
                agencia = "1218",
                numeroDaConta = "123456"
            )
        )
    }
}