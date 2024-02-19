package cn.llonvne.database.resolver

import cn.llonvne.database.repository.AuthenticationUserRepository
import cn.llonvne.entity.AuthenticationUser
import cn.llonvne.entity.role.TeamIdRole
import cn.llonvne.security.asJson
import cn.llonvne.security.check
import cn.llonvne.security.userRole
import org.springframework.stereotype.Service

/**
 * 用于从用户表查找对应权限的成员
 */
@Service
class GroupMembersResolver(private val userRepository: AuthenticationUserRepository) {
    /**
     * 查找一个具有[TeamIdRole]且匹配[TeamIdRole.teamId]的成员
     */
    suspend fun fromRole(need: TeamIdRole): List<AuthenticationUser> {
        val str = need.asJson
        val matchedUser = userRepository.matchRoleStr(str)
            .filter { user -> user.check(need) }
        return matchedUser
    }

    /**
     * 查找所有具有 [TeamIdRole.teamId] 权限的成员
     */
    suspend fun fromGroupId(groupId: Int): List<AuthenticationUser> {
        val matchedUser = userRepository
            .matchRoleStr(""""teamId":$groupId""".trimIndent())
            .filter {
                it.userRole.roles.filterIsInstance<TeamIdRole>().any { idRole -> idRole.teamId == groupId }
            }
        return matchedUser
    }
}