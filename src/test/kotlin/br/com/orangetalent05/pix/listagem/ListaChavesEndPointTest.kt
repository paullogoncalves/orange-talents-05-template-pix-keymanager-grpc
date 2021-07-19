package br.com.orangetalent05.pix.listagem

import br.com.orangetalent05.ListaChavesPixRequest
import br.com.orangetalent05.PixKeymanagerListaGrpcServiceGrpc
import br.com.orangetalent05.pix.*
import io.grpc.ManagedChannel
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.micronaut.context.annotation.Bean
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.containsInAnyOrder
import org.hamcrest.Matchers.hasSize
import io.micronaut.context.annotation.Factory
import io.micronaut.grpc.annotation.GrpcChannel
import io.micronaut.grpc.server.GrpcServerChannel
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.*

@MicronautTest(transactional = false)
internal class ListaChavesEndPointTest(val repo: ChavePixRepository,
val grpcClient: PixKeymanagerListaGrpcServiceGrpc.PixKeymanagerListaGrpcServiceBlockingStub
) {

    companion object {
        val CLIENT_ID = UUID.randomUUID()
    }

    @BeforeEach
    fun setup() {
        repo.save(chave(tipo = TipoDeChave.EMAIL, chave = "rafael.ponte@zup.com.br", clienteId = CLIENT_ID))
        repo.save(chave(tipo = TipoDeChave.ALEATORIA, chave = "chave-01", clienteId = UUID.randomUUID()))
        repo.save(chave(tipo = TipoDeChave.ALEATORIA, chave = "chave-02", clienteId = CLIENT_ID))
    }

    @Test
    fun `deve listar todas as chaves do cliente`() {

        val clienteId = CLIENT_ID.toString()

        val response = grpcClient.lista(ListaChavesPixRequest.newBuilder()
            .setClientId(clienteId)
            .build())

        with(response.chavesList) {
            assertThat(this, hasSize(2))
        }
    }

    @Test
    fun `nao deve listar as chaves quando o cliente nao possuir chaves`() {

        //cenario
        val clienteSemChave = UUID.randomUUID().toString()

        //acão
        val response = grpcClient.lista(ListaChavesPixRequest.newBuilder()
            .setClientId(clienteSemChave)
            .build())

        //validação
        with(response.chavesList) {
            assertThat(this, hasSize(0))
        }
        assertEquals(0, response.chavesCount)
    }

    @Test
    fun `nao deve listar todas as chaves do cliente quando clienteId for invalido`() {

        // cenário
        val clienteIdInvalido = ""

        // ação
        val thrown = assertThrows<StatusRuntimeException> {
            grpcClient.lista(ListaChavesPixRequest.newBuilder()
                .setClientId(clienteIdInvalido)
                .build())
        }

        // validação
        with(thrown) {
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
            assertEquals("Cliente ID não pode ser nulo ou vazio", status.description)
        }
    }

    @Factory
    class Clients  {
        @Bean
        fun blockingStub(@GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel): PixKeymanagerListaGrpcServiceGrpc.PixKeymanagerListaGrpcServiceBlockingStub? {
            return PixKeymanagerListaGrpcServiceGrpc.newBlockingStub(channel)
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
            conta =
            ContaAssociada(
                instituicao = "UNIBANCO ITAU",
                nomeDoTitular = "Rafael Ponte",
                cpfDoTitular = "12345678900",
                agencia = "1218",
                numeroDaConta = "123456"
            )
        )
    }
}