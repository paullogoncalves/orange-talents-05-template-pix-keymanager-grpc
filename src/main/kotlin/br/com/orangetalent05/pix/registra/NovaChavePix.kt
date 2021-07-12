package br.com.orangetalent05.pix.registra

import br.com.orangetalent05.pix.ChavePix
import br.com.orangetalent05.pix.ContaAssociada
import br.com.orangetalent05.pix.TipoDeChave
import br.com.orangetalent05.pix.TipoDeConta
import io.micronaut.core.annotation.Introspected
import org.hibernate.validator.constraints.br.CPF
import java.util.*
import javax.validation.constraints.Email
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size

@ValidPixKey
@Introspected
data class NovaChavePix(
    @field:NotBlank
    val clienteId: String?,
    @field:NotNull
    val tipo: TipoDeChave?,
    @field:Size(max = 77)
    val chave: String?,
    @field:NotNull
    val tipoDeConta: TipoDeConta?
) {

    fun toEntity(conta: ContaAssociada): ChavePix {
        return ChavePix(
            clientId = UUID.fromString(this.clienteId),
            tipo = TipoDeChave.valueOf(this.tipo!!.name),
            chave = if (this.tipo == TipoDeChave.ALEATORIA) UUID.randomUUID().toString() else this.chave!!,
            tipoDeConta = TipoDeConta.valueOf(this.tipoDeConta!!.name),
            conta = conta
        )
    }
}