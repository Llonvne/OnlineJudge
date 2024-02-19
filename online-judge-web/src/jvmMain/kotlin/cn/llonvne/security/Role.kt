@file:UseContextualSerialization

package cn.llonvne.security

import cn.llonvne.entity.AuthenticationUser
import cn.llonvne.entity.role.Role
import cn.llonvne.entity.role.TeamRole
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseContextualSerialization
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private val json = Json {
    encodeDefaults = true
}

fun normalUserRole() = json.encodeToString(UserRole.default())

inline fun <reified R : Role> List<Role>.check(required: R): Boolean {
    return map { provide ->
        required.check(provide)
    }.contains(true)
}

val Role.asJson: String
    get() = json.encodeToString(this)

inline fun <reified R : Role> AuthenticationUser.check(required: R): Boolean {
    return userRole.roles.check(required)
}

@Serializable
data class UserRole(val roles: List<Role> = listOf()) {

    companion object {
        fun default() = UserRole(
            TeamRole.default()
        )
    }

    override fun toString(): String {
        return roles.toString()
    }

    val asJson get() = json.encodeToString(this)
}

val AuthenticationUser.userRole: UserRole
    get() = fromUserRoleString(role = role) ?: UserRole.default()

fun fromUserRoleString(role: String): UserRole? = runCatching {
    json.decodeFromString<UserRole>(role)
}.getOrNull()