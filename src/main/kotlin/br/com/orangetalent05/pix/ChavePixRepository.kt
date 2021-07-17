package br.com.orangetalent05.pix

import io.micronaut.data.annotation.Repository
import io.micronaut.data.jpa.repository.JpaRepository
import java.util.*

@Repository
interface ChavePixRepository:JpaRepository<ChavePix, UUID> {

    fun existsByChave(chave: String?): Boolean

    fun findByIdAndClientId(pixId: UUID, clientId: UUID): Optional<ChavePix>

    fun findByChave(chave: String): Optional<ChavePix>

    fun findAllByClientId(clientId: UUID): List<ChavePix>


}