package cn.llonvne.database.resolver.group

import cn.llonvne.database.repository.UserRepository
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
class GroupMembersResolver(
    private val userRepository: UserRepository,
) {
    /**
     * 查找一个具有[TeamIdRole]且匹配[TeamIdRole.teamId]的成员
     * @param need 需要的权限
     * @return 匹配的成员
     * 该函数使用精确匹配在数据库内部查询权限，速度较快
     */
    suspend fun fromRole(need: TeamIdRole): List<AuthenticationUser> {
        val str = need.asJson
        val matchedUser = userRepository.matchRoleStr(str).filter { user -> user.check(need) }
        return matchedUser
    }

    /**
     * 查找一个具有[oneOf]中任何一个权限的成员并且匹配[TeamIdRole.teamId]
     * @param oneOf 需要的权限集合（匹配其中一个即可）
     * @return 匹配的成员
     * 该函数首先查找所有具有 [TeamIdRole.teamId] 权限的成员，然后再筛选出匹配的成员
     */
    suspend fun fromRoles(oneOf: List<TeamIdRole>): List<AuthenticationUser> {
        val groupId = oneOf.firstOrNull()?.teamId ?: return emptyList()
        val allMembers = fromGroupId(groupId)
        return allMembers.filter { user -> oneOf.any { role -> user.check(role) } }
    }

    /**
     * 查找所有具有 [TeamIdRole.teamId] 权限的成员
     */
    suspend fun fromGroupId(groupId: Int): List<AuthenticationUser> {
        val matchedUser =
            userRepository.matchRoleStr(""""teamId":$groupId""".trimIndent()).filter {
                it.userRole.roles
                    .filterIsInstance<TeamIdRole>()
                    .any { idRole -> idRole.teamId == groupId }
            }
        return matchedUser
    }
}
