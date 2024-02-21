package cn.llonvne.database.resolver

import cn.llonvne.database.repository.AuthenticationUserRepository
import cn.llonvne.entity.AuthenticationUser
import cn.llonvne.entity.group.GroupId
import cn.llonvne.entity.role.GroupManager
import cn.llonvne.entity.role.GroupOwner
import cn.llonvne.entity.role.TeamIdRole
import cn.llonvne.kvision.service.IGroupService.KickGroupResp
import cn.llonvne.kvision.service.IGroupService.KickGroupResp.*
import cn.llonvne.kvision.service.PermissionDeniedWithMessage
import cn.llonvne.kvision.service.RoleService
import cn.llonvne.security.check
import cn.llonvne.security.userRole
import org.springframework.stereotype.Service

@Service
class GroupKickResolver(
    private val userRepository: AuthenticationUserRepository,
    private val roleService: RoleService
) {
    suspend fun resolve(
        groupId: GroupId,
        groupIntId: Int,
        kicker: AuthenticationUser,
        kickedId: Int
    ): KickGroupResp {
        val kicked = userRepository.getByIdOrNull(kickedId) ?: return KickMemberNotFound(kickedId)
        val kickedRole =
            roleService.get(kickedId)?.groupIdRoles(groupIntId) ?: return KickMemberGroupIdRoleFound(kickedId, groupId)

        return if (kickedRole.filterIsInstance<GroupManager>().isNotEmpty()) {
            if (!kicker.check(GroupOwner.GroupOwnerImpl(groupIntId))) {
                PermissionDeniedWithMessage("您没有权限踢出管理员")
            } else {
                doKick(kicked, kickedRole)
            }
        } else {
            doKick(kicked, kickedRole)
        }
    }

    private suspend fun doKick(
        kicked: AuthenticationUser,
        kickedRole: List<TeamIdRole>
    ): Kicked {
        roleService.removeRole(kicked, kickedRole)
        return Kicked
    }
}