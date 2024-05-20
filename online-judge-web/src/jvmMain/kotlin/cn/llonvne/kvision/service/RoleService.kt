package cn.llonvne.kvision.service

import cn.llonvne.database.repository.RoleRepository
import cn.llonvne.entity.AuthenticationUser
import cn.llonvne.entity.role.Role
import cn.llonvne.security.UserLoginLogoutTokenValidator
import cn.llonvne.security.UserRole
import cn.llonvne.security.fromUserRoleString
import cn.llonvne.security.userRole
import org.springframework.stereotype.Service

@Service
class RoleService(
    private val roleRepository: RoleRepository,
    private val redisAuthentication: UserLoginLogoutTokenValidator
) {
    suspend fun addRole(userId: Int, vararg role: Role): Boolean {
        val userRoleStr = roleRepository.getRoleStrByUserId(userId) ?: return false
        val originRole = fromUserRoleString(userRoleStr) ?: UserRole.default()
        val newRole = UserRole((originRole.roles + role).toSet().toList())
        roleRepository.setRoleStrByUserId(userId, newRole)
        saveToRedis(userId, newRole)
        return true
    }

    private suspend fun saveToRedis(userId: Int, newRole: UserRole) {
        val redisUser = redisAuthentication.getAuthenticationUser(userId)
        if (redisUser != null) {
            redisAuthentication.update(redisUser.copy(role = newRole.asJson))
        }
    }

    suspend fun get(userId: Int): UserRole? {
        val roleStr = roleRepository.getRoleStrByUserId(userId) ?: return null
        return fromUserRoleString(roleStr) ?: UserRole.default()
    }

    suspend fun removeRole(user: AuthenticationUser, roles: List<Role>): Boolean {
        val userRoles = user.userRole.roles
        val newRoles = UserRole(userRoles.filter { it !in userRoles })
        roleRepository.setRoleStrByUserId(user.id, newRoles)
        saveToRedis(userId = user.id, newRoles)
        return true
    }

    suspend fun removeRole(userId: Int, roles: List<Role>): Boolean {
        val userRoleStr = roleRepository.getRoleStrByUserId(userId) ?: return false
        val originRole = fromUserRoleString(userRoleStr) ?: UserRole.default()
        val newRole = UserRole((originRole.roles - roles.toSet()).toList())
        roleRepository.setRoleStrByUserId(userId, newRole)
        saveToRedis(userId, newRole)
        return true
    }
}