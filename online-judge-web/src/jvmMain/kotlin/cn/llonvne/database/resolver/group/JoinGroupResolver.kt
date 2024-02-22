package cn.llonvne.database.resolver.group

import cn.llonvne.database.repository.GroupRepository
import cn.llonvne.database.resolver.group.JoinGroupVisibilityCheckResolver.JoinGroupVisibilityCheckResult.*
import cn.llonvne.entity.AuthenticationUser
import cn.llonvne.entity.group.GroupId
import cn.llonvne.entity.role.TeamIdRole
import cn.llonvne.entity.role.TeamMember
import cn.llonvne.kvision.service.GroupIdNotFound
import cn.llonvne.kvision.service.IGroupService.JoinGroupResp
import cn.llonvne.kvision.service.RoleService
import org.springframework.stereotype.Service

@Service
class JoinGroupResolver(
    private val groupRepository: GroupRepository,
    private val visibilityCheckResolver: JoinGroupVisibilityCheckResolver,
    private val roleService: RoleService,
    private val groupMembersResolver: GroupMembersResolver
) {
    suspend fun resolve(groupId: GroupId, id: Int, authenticationUser: AuthenticationUser): JoinGroupResp {
        val group = groupRepository.fromId(id) ?: return GroupIdNotFound(groupId)

        return when (visibilityCheckResolver.resolve(
            group.visibility, groupId
        )) {
            Accepted -> accept(authenticationUser, id, groupId)
            Waiting -> waiting(id, groupId)
            Rejected -> reject(groupId)
        }
    }

    private suspend fun reject(groupId: GroupId): JoinGroupResp {
        return JoinGroupResp.Reject(groupId)
    }

    private suspend fun accept(authenticationUser: AuthenticationUser, id: Int, groupId: GroupId): JoinGroupResp {
        roleService.addRole(authenticationUser.id, TeamMember.TeamMemberImpl(id))
        return JoinGroupResp.Joined(groupId)
    }

    private suspend fun waiting(id: Int, groupId: GroupId): JoinGroupResp {
        // 找到组管理员
        val managers = groupMembersResolver.fromRoles(TeamIdRole.getManagerRoles(id))

        if (managers.isEmpty()) {
            return JoinGroupResp.NoManagersFound(groupId = groupId)
        } else {
            TODO()
        }
    }
}