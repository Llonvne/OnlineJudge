package cn.llonvne.security

import kotlinx.serialization.Serializable

@Serializable
data class AuthenticationToken(
    val username:String,
    private val token: String
)