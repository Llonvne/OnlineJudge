package cn.llonvne.security

import kotlinx.serialization.Serializable

@Serializable
data class AuthenticationToken(
    private val token: String
)