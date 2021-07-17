package br.com.orangetalent05.pix.consulta

import br.com.orangetalent05.ConsultaChavePixResponse
import br.com.orangetalent05.TipoDeChave
import br.com.orangetalent05.TipoDeConta
import com.google.protobuf.Timestamp
import java.time.ZoneId
import javax.inject.Singleton


class ConsultaChavePixConverter() {

    fun convert(chavePixInfo: ChavePixInfo): ConsultaChavePixResponse {
        return ConsultaChavePixResponse.newBuilder()
            .setClienteId(chavePixInfo.clienteId?.toString() ?: "")
            .setPixId(chavePixInfo.pixId?.toString() ?: "")
            .setChave(ConsultaChavePixResponse.ChavePix
                .newBuilder()
                .setTipo(TipoDeChave.valueOf(chavePixInfo.tipo.name))
                .setChave(chavePixInfo.chave)
                .setConta(ConsultaChavePixResponse.ChavePix.ContaInfo.newBuilder()
                    .setTipo(TipoDeConta.valueOf(chavePixInfo.tipoDeConta.name))
                    .setInstituicao(chavePixInfo.conta.instituicao)
                    .setNomeDoTitular(chavePixInfo.conta.nomeDoTitular)
                    .setCpfDoTitular(chavePixInfo.conta.cpfDoTitular)
                    .setAgencia(chavePixInfo.conta.agencia)
                    .setNumeroDaConta(chavePixInfo.conta.numeroDaConta)
                    .build()
                )
                .setCriadaEm(chavePixInfo.registradaEm.let {
                    val createdAt = it.atZone(ZoneId.of("UTC")).toInstant()
                    Timestamp.newBuilder()
                        .setSeconds(createdAt.epochSecond)
                        .setNanos(createdAt.nano)
                        .build()
                })
            ) .build()
    }
}
