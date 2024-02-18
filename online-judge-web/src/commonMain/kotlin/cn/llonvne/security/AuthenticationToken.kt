package cn.llonvne.security

import kotlinx.serialization.Serializable

@Serializable
sealed interface AuthenticationToken {
    val token: String
    val id: Int
}

@Serializable
data class RedisToken(override val id: Int, override val token: String) : AuthenticationToken {
    override fun toString(): String {
        return "<Redis-Token-$id>"
    }
}