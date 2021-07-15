package br.com.orangetalent05.pix.registra

import br.com.orangetalent05.PixKeymanagerGrpcServiceGrpc
import br.com.orangetalent05.RegistraChavePixRequest
import br.com.orangetalent05.TipoDeChave
import br.com.orangetalent05.TipoDeConta
import br.com.orangetalent05.integration.bcb.*
import br.com.orangetalent05.integration.itau.DadosDaContaResponse
import br.com.orangetalent05.integration.itau.InstituicaoResponse
import br.com.orangetalent05.integration.itau.ItauClient
import br.com.orangetalent05.integration.itau.Titular
import br.com.orangetalent05.pix.ChavePix
import br.com.orangetalent05.pix.ChavePixRepository
import br.com.orangetalent05.pix.ContaAssociada
import br.com.orangetalent05.utils.violations
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
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.containsInAnyOrder
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import java.time.LocalDateTime
import java.util.*
import javax.inject.Inject

@MicronautTest(transactional = false)
internal class RegistraChaveEndPointTest(
    val repository: ChavePixRepository,
    val grpcClient: PixKeymanagerGrpcServiceGrpc.PixKeymanagerGrpcServiceBlockingStub,
) {

    @Inject
    lateinit var itauClient: ItauClient

    @Inject
    lateinit var bancoCentralClient: BancoCentralClient

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

        `when`(bancoCentralClient.insert(createPixKeyRequest()))
            .thenReturn(HttpResponse.created(createPixKeyResponse()))

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

    @Test
    fun `nao deve registrar chave pix quando parametros forem invalidos - chave invalida`() {

        val thrown = assertThrows<StatusRuntimeException> {
            grpcClient.registra(RegistraChavePixRequest.newBuilder()
                .setClienteId(CLIENT_ID.toString())
                .setTipoDeChave(TipoDeChave.CPF)
                .setChave("378.930.cpf-invalido.389-73")
                .setTipoDeConta(TipoDeConta.CONTA_POUPANCA)
                .build())
        }

        with(thrown) {
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
            assertEquals("Dados inválidos", status.description)
            assertThat(violations(), containsInAnyOrder(
                Pair("chave", "chave Pix inválida (CPF)"),
            ))
        }
    }

    @MockBean(ItauClient::class)
    fun itauClient(): ItauClient? {
        return Mockito.mock(ItauClient::class.java)
    }

    @MockBean(BancoCentralClient::class)
    fun bancoCentralClient(): BancoCentralClient? {
        return Mockito.mock(BancoCentralClient::class.java)
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

     fun createPixKeyRequest(): CreatePixRequest {
        return CreatePixRequest(
            keyType = PixKeyType.EMAIL,
            key = "rponte@gmail.com",
            bankAccount = bankAccount(),
            owner = owner()
        )
    }

    private fun createPixKeyResponse(): CreatePixResponse {
        return CreatePixResponse(
            keyType = PixKeyType.EMAIL,
            key = "rponte@gmail.com",
            bankAccount = bankAccount(),
            owner = owner(),
            createdAt = LocalDateTime.now()
        )
    }

    private fun bankAccount(): BankAccount {
        return BankAccount(
            participant = ContaAssociada.ITAU_UNIBANCO_ISPB,
            branch = "1218",
            accountNumber = "291900",
            accountType = BankAccount.AccountType.CACC
        )
    }

    private fun owner(): Owner {
        return Owner(
            type = Owner.OwnerType.NATURAL_PERSON,
            name = "Rafael Ponte",
            taxIdNumber = "63657520325"
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





