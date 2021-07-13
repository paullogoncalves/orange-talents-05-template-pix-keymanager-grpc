package br.com.orangetalent05.integration.itau

import br.com.orangetalent05.pix.ContaAssociada

data class DadosDaContaResponse(
    val tipo: String,
    val instituicao: InstituicaoResponse,
    val agencia: String,
    val numero: String,
    val titular: Titular
) {

    fun toEntity(): ContaAssociada {
        return  ContaAssociada(
            instituicao = this.instituicao.nome,
            nomeDoTitular = this.titular.nome,
            cpfDoTitular = this.titular.cpf,
            agencia = this.agencia,
            numeroDaConta = this.numero
        )
    }
}

data class InstituicaoResponse(val nome: String, val ispb: String)
data class Titular(val nome: String, val cpf: String)