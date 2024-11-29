package cn.llonvne.security

import kotlinx.serialization.Serializable

@Serializable
sealed interface Token {
    val token: String
    val id: Int
}

@Serializable
data class RedisToken(
    override val id: Int,
    override val token: String,
) : Token {
    override fun toString(): String = "<Redis-Token-$id>"
}
