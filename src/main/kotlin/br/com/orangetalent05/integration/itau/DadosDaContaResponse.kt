package br.com.orangetalent05.integration.itau

data class DadosDaContaResponse(
    val tipo: String,
    val instituicao: InstituicaoResponse,
    val agencia: String,
    val numero: String,
    val titular: Titular
) {

}

data class InstituicaoResponse(val nome: String, val ispb: String)
data class Titular(val id: String, val nome: String, val cpf: String)