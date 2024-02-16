package cn.llonvne.entity.group

import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable

@Serializable
data class Group(
    val groupId: Int? = null, val groupName: String, val groupHash: String,

    val version: Int? = null, val createdAt: LocalDateTime? = null, val updatedAt: LocalDateTime? = null
)

@Serializable
data class GroupUser(
    val groupUserId: Int? = null, val authenticationUserId: Int,

    val groupName: String, val userType: GroupUserType,

    val version: Int? = null, val createdAt: LocalDateTime? = null, val updatedAt: LocalDateTime? = null
)

@Serializable
data class GroupUserRole(
    val roleId: Int? = null,
    val groupUserId: Int,
    val roleKey: GroupRole
)

enum class GroupRole {

}

@Serializable
enum class GroupUserType {
    ROOT, ADMIN, CUSTOM;

    fun decr() = when (this) {
        ROOT -> "所有者"
        ADMIN -> "管理员"
        CUSTOM -> "自定义"
    }
}



