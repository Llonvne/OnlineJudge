package cn.llonvne.kvision.service

import cn.llonvne.database.repository.RoleRepository
import cn.llonvne.entity.AuthenticationUser
import cn.llonvne.entity.role.Role
import cn.llonvne.entity.role.TeamIdRole
import cn.llonvne.entity.role.TeamMember
import cn.llonvne.security.UserRole
import cn.llonvne.security.fromUserRoleString
import cn.llonvne.security.userRole
import org.springframework.stereotype.Service

@Service
class RoleService(
    private val roleRepository: RoleRepository
) {
    suspend fun addRole(userId: Int, vararg role: Role): Boolean {
        val userRoleStr = roleRepository.getRoleStrByUserId(userId) ?: return false
        val userRole = fromUserRoleString(userRoleStr) ?: UserRole.default()
        val roles = (userRole.roles + role).toSet().toList()
        roleRepository.setRoleStrByUserId(userId, UserRole(roles))
        return true
    }

    suspend fun get(userId: Int): UserRole? {
        val roleStr = roleRepository.getRoleStrByUserId(userId) ?: return null
        return fromUserRoleString(roleStr) ?: UserRole.default()
    }

    suspend fun removeRole(user: AuthenticationUser, roles: List<Role>): Boolean {
        val roles = get(user.id)?.roles?.toSet() ?: return false
        val newRoles = roles.filter { it !in roles }
        roleRepository.setRoleStrByUserId(user.id, UserRole(newRoles))
        return true
    }
}