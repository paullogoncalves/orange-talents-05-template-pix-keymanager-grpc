package br.com.orangetalent05.pix.remove

import br.com.orangetalent05.integration.itau.ItauClient
import br.com.orangetalent05.pix.ChavePixRepository
import br.com.orangetalent05.pix.exceptions.ChavePixNaoEncontradaException
import io.micronaut.validation.Validated
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton
import javax.transaction.Transactional
import javax.validation.constraints.NotBlank

@Validated
@Singleton
class RemoveChaveService(
    @Inject val chaveRepo: ChavePixRepository
) {

    @Transactional
    fun remove(@NotBlank clientId: String?, @NotBlank pixId: String?) {

        val uClientId = UUID.fromString(clientId)
        val uPixId = UUID.fromString(pixId)

        val chave = chaveRepo.findByIdAndClientId(uPixId, uClientId)

        if( chave.isEmpty) {
            throw ChavePixNaoEncontradaException("Chave não encontrada")
        }

        chaveRepo.deleteById(uPixId)
    }
}
