package br.com.orangetalent05.pix.registra

import br.com.orangetalent05.PixKeymanagerGrpcServiceGrpc
import br.com.orangetalent05.RegistraChavePixRequest
import br.com.orangetalent05.TipoDeChave
import br.com.orangetalent05.TipoDeConta
import br.com.orangetalent05.integration.itau.DadosDaContaResponse
import br.com.orangetalent05.integration.itau.InstituicaoResponse
import br.com.orangetalent05.integration.itau.ItauClient
import br.com.orangetalent05.integration.itau.Titular
import br.com.orangetalent05.pix.ChavePix
import br.com.orangetalent05.pix.ChavePixRepository
import br.com.orangetalent05.pix.ContaAssociada
import io.grpc.ManagedChannel
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.micronaut.context.annotation.Bean
import io.micronaut.context.annotation.Factory
import io.micronaut.grpc.annotation.GrpcChannel
import io.micronaut.grpc.server.GrpcServerChannel
import io.micronaut.http.HttpResponse
import io.micronaut.test.annotation.MockBean
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import java.util.*
import javax.inject.Inject

@MicronautTest(transactional = false)
internal class RegistraChaveEndPointTest(
    val repository: ChavePixRepository,
    val grpcClient: PixKeymanagerGrpcServiceGrpc.PixKeymanagerGrpcServiceBlockingStub,
) {

    @Inject
    lateinit var itauClient: ItauClient;

    companion object {
        val CLIENT_ID = UUID.randomUUID()
    }

    @BeforeEach
    fun setup() {
        repository.deleteAll()
    }

    @Test
    fun `deve registrar nova chave pix`() {

        `when`(itauClient.buscaPorTipo(clientId = CLIENT_ID.toString(), tipo = "CONTA_CORRENTE"))
            .thenReturn(HttpResponse.ok(dadosDaContaResponse()))


        val response = grpcClient.registra(RegistraChavePixRequest.newBuilder()
                                 .setClienteId(CLIENT_ID.toString())
                                 .setTipoDeChave(TipoDeChave.EMAIL)
                                 .setChave("rponte@gmail.com")
                                 .setTipoDeConta(TipoDeConta.CONTA_CORRENTE)
                                 .build())

        with(response) {
            assertEquals(CLIENT_ID.toString(), clienteId)
            assertNotNull(pixId)
        }
    }

    @Test
    fun `nao deve reagistrar chave se ja existir`() {
        repository.save(chave(
            tipo = br.com.orangetalent05.pix.TipoDeChave.CPF,
            chave = "56565198055",
            clienteId = CLIENT_ID
        ))

        val response = assertThrows<StatusRuntimeException> {
            grpcClient.registra(RegistraChavePixRequest.newBuilder()
                .setClienteId(CLIENT_ID.toString())
                .setTipoDeChave(TipoDeChave.CPF)
                .setChave("56565198055")
                .setTipoDeConta(TipoDeConta.CONTA_CORRENTE)
                .build())
        }

        with(response) {
            assertEquals(Status.ALREADY_EXISTS.code, status.code)
            assertEquals("Chave Pix '56565198055' existente", status.description)
        }

    }
    @MockBean(ItauClient::class)
    fun itauClient(): ItauClient? {
        return Mockito.mock(ItauClient::class.java)
    }

    

    @Factory
    class Clients {
        @Bean
        fun blockingStub(@GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel): PixKeymanagerGrpcServiceGrpc.PixKeymanagerGrpcServiceBlockingStub? {
            return PixKeymanagerGrpcServiceGrpc.newBlockingStub(channel)
        }
    }
    private fun dadosDaContaResponse(): DadosDaContaResponse {
        return DadosDaContaResponse(
            tipo = "CONTA_CORRENTE",
            instituicao = InstituicaoResponse("UNIBANCO ITAU SA", ContaAssociada.ITAU_UNIBANCO_ISPB),
            agencia = "1218",
            numero = "291900",
            titular = Titular("Rafael Ponte", "63657520325")
        )
    }
    private fun chave(
        tipo: br.com.orangetalent05.pix.TipoDeChave,
        chave: String = UUID.randomUUID().toString(),
        clienteId: UUID = UUID.randomUUID(),
    ): ChavePix {
        return ChavePix(
            clientId = clienteId,
            tipo = tipo,
            chave = chave,
            tipoDeConta = br.com.orangetalent05.pix.TipoDeConta.CONTA_CORRENTE,
            conta = ContaAssociada(
                instituicao = "UNIBANCO ITAU",
                nomeDoTitular = "Rafael Ponte",
                cpfDoTitular = "56565198055",
                agencia = "1218",
                numeroDaConta = "291900"
            )
        )
    }
}





