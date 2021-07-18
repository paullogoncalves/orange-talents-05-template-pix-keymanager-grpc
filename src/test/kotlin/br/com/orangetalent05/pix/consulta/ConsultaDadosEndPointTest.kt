package br.com.orangetalent05.pix.consulta

import br.com.orangetalent05.ConsultaChavePixRequest
import br.com.orangetalent05.ConsultaChavePixRequest.FiltroPorPixId
import br.com.orangetalent05.PixKeymanagerConsultaGrpcServiceGrpc
import br.com.orangetalent05.integration.bcb.*
import br.com.orangetalent05.pix.*
import br.com.orangetalent05.pix.TipoDeChave.*
import br.com.orangetalent05.utils.violations
import io.grpc.ManagedChannel
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.micronaut.context.annotation.Bean
import io.micronaut.context.annotation.Factory
import io.micronaut.grpc.annotation.GrpcChannel
import io.micronaut.grpc.server.GrpcServerChannel
import io.micronaut.test.annotation.MockBean
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.containsInAnyOrder
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito
import java.time.LocalDateTime
import java.util.*
import javax.inject.Inject

@MicronautTest(transactional = false)
internal class ConsultaDadosEndPointTest(
    val repository: ChavePixRepository,
    val grpcClient: PixKeymanagerConsultaGrpcServiceGrpc.PixKeymanagerConsultaGrpcServiceBlockingStub,
){

    @Inject
    lateinit var clientBcb: BancoCentralClient

    companion object {
        val CLIENT_ID = UUID.randomUUID()
    }

    @BeforeEach
    fun setup() {
        repository.save(chave(tipo = EMAIL, chave = "rafael.ponte@zup.com.br", clienteId = CLIENT_ID))
        repository.save(chave(tipo = CPF, chave = "63657520325", clienteId = UUID.randomUUID()))
        repository.save(chave(tipo = ALEATORIA, chave = "randomkey-3", clienteId = CLIENT_ID))
        repository.save(chave(tipo = CELULAR, chave = "+55988675344", clienteId = CLIENT_ID))
    }
    @AfterEach
    fun cleanUp() {
        repository.deleteAll()
    }

    @Test
    fun`deve consultar os dados da chave por pixId e clientId`() {
        //cenario
        val chaveNobanco = repository.findByChave("+55988675344").get()

        //ação
        val response = grpcClient.busca(ConsultaChavePixRequest.newBuilder()
            .setPixId(FiltroPorPixId.newBuilder()
                .setPixId(chaveNobanco.id.toString())
                .setClienteId(chaveNobanco.clientId.toString())
                .build())
            .build())

        //validação
        with(response) {
            assertEquals(chaveNobanco.id.toString(), this.pixId)
            assertEquals(chaveNobanco.clientId.toString(), this.clienteId)
            assertEquals(chaveNobanco.tipo.name, this.chave.tipo.name)
            assertEquals(chaveNobanco.chave, this.chave.chave)

        }
    }

    @MockBean(BancoCentralClient::class)
    fun bancoCentralClient(): BancoCentralClient?{
        return Mockito.mock(BancoCentralClient::class.java)
    }
    @Test
    fun `nao deve carregar chave por pixId e clienteId quando filtro invalido`() {
        // ação
        val thrown = assertThrows<StatusRuntimeException> {
            grpcClient.busca(ConsultaChavePixRequest.newBuilder()
                .setPixId(FiltroPorPixId.newBuilder()
                    .setPixId("")
                    .setClienteId("")
                    .build()
                ).build())
        }

        // validação
        with(thrown) {
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
            assertEquals("Dados inválidos", status.description)
            assertThat(violations(), containsInAnyOrder(
                Pair("pixId", "must not be blank"),
                Pair("clienteId", "must not be blank"),
                Pair("pixId", "não é um formato válido de UUID"),
                Pair("clienteId", "não é um formato válido de UUID"),
            ))
        }
    }


    @Factory
    class Clients {
        @Bean
        fun blockingStub(@GrpcChannel(GrpcServerChannel.NAME) chanel: ManagedChannel):
                PixKeymanagerConsultaGrpcServiceGrpc.PixKeymanagerConsultaGrpcServiceBlockingStub{
            return PixKeymanagerConsultaGrpcServiceGrpc.newBlockingStub(chanel)
        }
    }

    private fun chave(
        tipo: TipoDeChave,
        chave: String = UUID.randomUUID().toString(),
        clienteId: UUID = UUID.randomUUID(),
    ): ChavePix {
        return ChavePix(
            clientId = clienteId,
            tipo = tipo,
            chave = chave,
            tipoDeConta = TipoDeConta.CONTA_CORRENTE,
            conta = ContaAssociada(
                instituicao = "UNIBANCO ITAU",
                nomeDoTitular = "Rafael Ponte",
                cpfDoTitular = "12345678900",
                agencia = "1218",
                numeroDaConta = "123456"
            )
        )
    }

    private fun pixKeyDetailsResponse(): PixKeyDetailsResponse {
        return PixKeyDetailsResponse(
            keyType = PixKeyType.EMAIL,
            key = "user.from.another.bank@santander.com.br",
            bankAccount = bankAccount(),
            owner = owner(),
            createdAt = LocalDateTime.now()
        )
    }

    private fun bankAccount(): BankAccount {
        return BankAccount(
            participant = "90400888",
            branch = "9871",
            accountNumber = "987654",
            accountType = BankAccount.AccountType.SVGS
        )
    }

    private fun owner(): Owner {
        return Owner(
            type = Owner.OwnerType.NATURAL_PERSON,
            name = "Another User",
            taxIdNumber = "12345678901"
        )
    }

}
