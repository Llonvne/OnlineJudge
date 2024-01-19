package cn.llonvne.dtos

import kotlinx.serialization.Serializable

@Serializable
data class AuthenticationUserDto(
    val username: String
)