package cn.llonvne.security

import kotlinx.serialization.Serializable

@Serializable
data class AuthenticationToken(
    val username: String,
    val token: String,
    val authenticationUserId: Int
)