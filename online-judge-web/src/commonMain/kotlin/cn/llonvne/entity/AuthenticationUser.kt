package cn.llonvne.entity

import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable
import kotlin.Int


/**
 * 注册用户实体
 * @see [cn.llonvne.database.entity.def.AuthenticationUserDef]
 */
@Serializable
data class AuthenticationUser(
    val id: Int = 0,
    val username: String,
    val encryptedPassword: String,
    val version: Int? = null,
    val createdAt: LocalDateTime? = null,
    val updatedAt: LocalDateTime? = null
) {
    companion object
}


