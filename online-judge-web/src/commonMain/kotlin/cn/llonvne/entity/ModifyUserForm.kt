package cn.llonvne.entity

import kotlinx.serialization.Serializable

@Serializable
data class ModifyUserForm(
    val userId: String,
    val username: String,
    val isBanned: Boolean,
)
