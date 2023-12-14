package cn.llonvne.database.entity

import org.komapper.annotation.*
import java.time.LocalDateTime

@KomapperEntity
data class User(
    @KomapperId @KomapperAutoIncrement
    val id: Int = 0,
    val username: String,
    val password: String,
    @KomapperVersion
    val version: Int = 0,
    @KomapperCreatedAt
    val createdAt: LocalDateTime = LocalDateTime.MIN,
    @KomapperUpdatedAt
    val updatedAt: LocalDateTime = LocalDateTime.MIN,
)