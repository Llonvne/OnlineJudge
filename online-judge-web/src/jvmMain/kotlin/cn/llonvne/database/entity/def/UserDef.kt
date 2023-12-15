package cn.llonvne.database.entity.def

import cn.llonvne.entity.User
import org.komapper.annotation.*

@KomapperEntityDef(User::class)
data class UserDef(
    @KomapperId @KomapperAutoIncrement
    val id: Nothing,
    val username: Nothing,
    val password: Nothing,
    @KomapperVersion
    val version: Nothing,
    @KomapperCreatedAt
    val createdAt: Nothing,
    @KomapperUpdatedAt
    val updatedAt: Nothing,
)