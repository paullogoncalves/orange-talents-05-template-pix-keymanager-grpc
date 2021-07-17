package br.com.orangetalent05.pix.consulta

import br.com.orangetalent05.pix.ChavePix
import br.com.orangetalent05.pix.ContaAssociada
import br.com.orangetalent05.pix.TipoDeChave
import br.com.orangetalent05.pix.TipoDeConta
import java.time.LocalDateTime
import java.util.*

data class ChavePixInfo(
    val pixId: UUID? = null,
    val clienteId: UUID? = null,
    val tipo: TipoDeChave,
    val chave: String,
    val tipoDeConta: TipoDeConta,
    val conta: ContaAssociada,
    val registradaEm: LocalDateTime = LocalDateTime.now()
) {

    companion object {
        fun of(chave: ChavePix): ChavePixInfo {
            return ChavePixInfo(
                pixId = chave.id,
                clienteId = chave.clientId,
                tipo = chave.tipo,
                chave = chave.chave,
                tipoDeConta = chave.tipoDeConta,
                conta = chave.conta,
                registradaEm = chave.criadoEm
            )
        }
    }
}