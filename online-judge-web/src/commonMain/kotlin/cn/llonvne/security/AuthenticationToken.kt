package cn.llonvne.security

import kotlinx.serialization.Serializable

@Serializable
sealed interface AuthenticationToken {
    val token: String
}


