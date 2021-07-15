package br.com.orangetalent05.pix.remove

import br.com.orangetalent05.integration.bcb.BancoCentralClient
import br.com.orangetalent05.integration.bcb.DeletePixKeyRequest
import br.com.orangetalent05.integration.itau.ItauClient
import br.com.orangetalent05.pix.ChavePixRepository
import br.com.orangetalent05.pix.exceptions.ChavePixNaoEncontradaException
import io.micronaut.http.HttpStatus
import io.micronaut.validation.Validated
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton
import javax.transaction.Transactional
import javax.validation.constraints.NotBlank

@Validated
@Singleton
class RemoveChaveService(
    @Inject val chaveRepo: ChavePixRepository,
    @Inject val bancoCentralClient: BancoCentralClient
) {

    @Transactional
    fun remove(@NotBlank clientId: String?, @NotBlank pixId: String?) {

        val uClientId = UUID.fromString(clientId)
        val uPixId = UUID.fromString(pixId)

        val chave = chaveRepo.findByIdAndClientId(uPixId, uClientId)
            .orElseThrow { ChavePixNaoEncontradaException("Chave Pix não encontrada ou não pertence ao cliente") }

//        if( chave.isEmpty) {
//            throw ChavePixNaoEncontradaException("Chave não encontrada")
//        }

        val request = DeletePixKeyRequest(chave.chave)

        val response = bancoCentralClient.delete(key = chave.chave, request = request) // 1
        if (response.status != HttpStatus.OK) { // 1
            throw IllegalStateException("Erro ao remover chave Pix no Banco Central do Brasil (BCB)")
        }

        chaveRepo.deleteById(uPixId)

    }
}
