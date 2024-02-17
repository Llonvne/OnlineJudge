package cn.llonvne.security

import cn.llonvne.entity.AuthenticationUser
import cn.llonvne.entity.role.Role
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.reflect.full.isSubclassOf

private val json = Json

fun normalUserRole() = json.encodeToString(UserRole.empty())

interface RoleCheckDsl {
    fun require(role: Role)

    fun require(roles: List<Role>)

    fun provide(role: Role)

    fun provide(userRole: UserRole)
}

private class RoleCheckDslImpl : RoleCheckDsl {

    private val requiredRoles = mutableListOf<Role>()

    private val provideRoles = mutableListOf<Role>()

    override fun require(role: Role) {
        requiredRoles.add(role)
    }

    override fun require(roles: List<Role>) {
        requiredRoles += roles
    }

    override fun provide(role: Role) {
        provideRoles.add(role)
    }

    override fun provide(userRole: UserRole) {
        provideRoles += userRole.roles
    }

    private fun check(role: Role): Boolean {
        return provideRoles.any {
//            role.given(it)
            role::class.isSubclassOf(role::class)
        }
    }

    fun result(): Boolean {
        return requiredRoles.all { check(it) }
    }
}

fun check(action: RoleCheckDsl.() -> Unit): Boolean {
    val roleCheckDsl = RoleCheckDslImpl()
    roleCheckDsl.action()
    return roleCheckDsl.result()
}

@Serializable
data class UserRole(val roles: List<Role> = listOf()) {
    companion object {
        fun empty() = UserRole()
    }
}

val AuthenticationUser.userRole: UserRole
    get() = runCatching {
        json.decodeFromString<UserRole>(role)
    }.getOrNull() ?: UserRole.empty()