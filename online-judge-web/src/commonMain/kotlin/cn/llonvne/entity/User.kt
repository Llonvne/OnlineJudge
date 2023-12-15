package cn.llonvne.entity

import kotlinx.datetime.LocalDateTime


data class User(
    val id: Int = 0,
    val username: String,
    val password: String,
    val version: Int = 0,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)