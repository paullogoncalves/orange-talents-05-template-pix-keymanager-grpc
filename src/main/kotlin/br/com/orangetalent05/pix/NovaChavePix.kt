package br.com.orangetalent05.pix

import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull

class NovaChavePix(
    @field:NotBlank
    val clienteId: String?,
    @field:NotNull
    val tipoDeChave: TipoDeChave?,
    @field:NotBlank
    val chave: String?,
    @field:NotNull
    val tipoDeConta: TipoDeConta?
) {
}