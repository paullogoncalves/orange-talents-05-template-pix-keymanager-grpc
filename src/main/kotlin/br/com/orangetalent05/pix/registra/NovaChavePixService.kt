package br.com.orangetalent05.pix.registra

import br.com.orangetalent05.integration.bcb.BancoCentralClient
import br.com.orangetalent05.integration.bcb.CreatePixRequest
import br.com.orangetalent05.integration.itau.ItauClient
import br.com.orangetalent05.pix.ChavePix
import br.com.orangetalent05.pix.ChavePixRepository
import br.com.orangetalent05.pix.exceptions.ChavePixExistenteException
import io.micronaut.http.HttpStatus
import io.micronaut.validation.Validated
import org.slf4j.LoggerFactory
import javax.inject.Inject
import javax.inject.Singleton
import javax.transaction.Transactional
import javax.validation.Valid

@Validated
@Singleton
class NovaChavePixService(
    @Inject val itauClient: ItauClient,
    @Inject val chaveRepo: ChavePixRepository,
    @Inject val bancoCentralClient: BancoCentralClient
) {

    private val LOGGER = LoggerFactory.getLogger(this::class.java)

    @Transactional
    fun registra(@Valid novaChave: NovaChavePix): ChavePix   {

        if (chaveRepo.existsByChave(novaChave.chave)) {
            throw ChavePixExistenteException("Chave Pix '${novaChave.chave}' existente")

        }
        val itauResponse = itauClient.buscaPorTipo(novaChave.clienteId!!, novaChave.tipoDeConta!!.name)
        val conta = itauResponse.body()?.toEntity()?: throw IllegalStateException("Cliente não encontrado")

        val chave = novaChave.toEntity(conta)
        chaveRepo.save(chave)
        LOGGER.info("Foi gravado no banco a chave: ${chave.chave}")

        val requestBcb = CreatePixRequest.of(chave).also {
            LOGGER.info("Registrando chave no banco central $it")
        }

        val responseBcb = bancoCentralClient.insert(requestBcb)
        if (responseBcb.status != HttpStatus.CREATED) {
            throw IllegalStateException("Erro ao cria chave pix no Banco central")
        }

        chave.atualiza(responseBcb.body().key)

        return chave
    }
}