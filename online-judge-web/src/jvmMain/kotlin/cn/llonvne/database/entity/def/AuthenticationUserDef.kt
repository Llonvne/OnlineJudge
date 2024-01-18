package cn.llonvne.database.entity.def

import cn.llonvne.entity.AuthenticationUser
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import org.komapper.annotation.*
import java.io.IOException


@KomapperEntityDef(AuthenticationUser::class)
private data class AuthenticationUserDef(
    @KomapperId @KomapperAutoIncrement
    val id: Nothing,
    val username: Nothing,
    val encryptedPassword: Nothing,
    @KomapperVersion
    val version: Nothing,
    @KomapperCreatedAt
    val createdAt: Nothing,
    @KomapperUpdatedAt
    val updatedAt: Nothing,
)
