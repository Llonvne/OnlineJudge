package cn.llonvne.kvision.service

import cn.llonvne.database.repository.RoleRepository
import cn.llonvne.entity.role.Role
import cn.llonvne.security.UserRole
import cn.llonvne.security.fromUserRoleString
import org.springframework.stereotype.Service

@Service
class RoleService(
    private val roleRepository: RoleRepository
) {
    suspend fun addRole(userId: Int, vararg role: Role): Boolean {
        val userRoleStr = roleRepository.getRoleStrByUserId(userId) ?: return false
        val userRole = fromUserRoleString(userRoleStr) ?: UserRole.default()
        roleRepository.setRoleStrByUserId(userId, UserRole(userRole.roles + role))
        return true
    }

    suspend fun get(userId: Int): UserRole? {
        val roleStr = roleRepository.getRoleStrByUserId(userId) ?: return null
        return fromUserRoleString(roleStr) ?: UserRole.default()
    }
}