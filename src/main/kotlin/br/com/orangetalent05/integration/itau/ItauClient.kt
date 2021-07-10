package br.com.orangetalent05.integration.itau

import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.PathVariable
import io.micronaut.http.annotation.QueryValue
import io.micronaut.http.client.annotation.Client


@Client("http://localhost:9091")
interface ItauClient {

    @Get("/api/v1/clientes/{clientId}/contas{?tipo}")
    fun buscaPorTipo(@PathVariable clientId: String, @QueryValue tipo: String ): HttpResponse<Any>
}