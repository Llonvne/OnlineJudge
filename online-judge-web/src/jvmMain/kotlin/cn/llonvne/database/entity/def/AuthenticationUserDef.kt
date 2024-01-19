package cn.llonvne.database.entity.def

import cn.llonvne.entity.AuthenticationUser
import org.komapper.annotation.*


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
