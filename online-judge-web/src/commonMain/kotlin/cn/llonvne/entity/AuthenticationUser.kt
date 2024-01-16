package cn.llonvne.entity

import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable


/**
 * 注册用户实体
 * @see [cn.llonvne.database.entity.def.AuthenticationUserDef]
 */
@Serializable
data class AuthenticationUser(
    val id: Int = 0,
    val username: String,
    val encryptedPassword: String,
    val version: Int = 0,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
) {
    companion object
}

