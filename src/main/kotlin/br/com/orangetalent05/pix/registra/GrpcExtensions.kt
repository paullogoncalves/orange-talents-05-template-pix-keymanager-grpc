package br.com.orangetalent05.pix.registra

import br.com.orangetalent05.RegistraChavePixRequest
import br.com.orangetalent05.TipoDeChave.*
import br.com.orangetalent05.TipoDeConta.*
import br.com.orangetalent05.pix.TipoDeChave
import br.com.orangetalent05.pix.TipoDeConta


fun RegistraChavePixRequest.toModel(): NovaChavePix {
    return NovaChavePix(
        clienteId = clienteId,
        tipo = when (tipoDeChave) {
            UNKNOWN_TIPO_CHAVE -> null
            else -> TipoDeChave.valueOf(tipoDeChave.name)
        },
        chave = chave,
        tipoDeConta = when (tipoDeConta) {
            UNKNOWN_TIPO_CONTA -> null
            else -> TipoDeConta.valueOf(tipoDeConta.name)
        }
    )
}