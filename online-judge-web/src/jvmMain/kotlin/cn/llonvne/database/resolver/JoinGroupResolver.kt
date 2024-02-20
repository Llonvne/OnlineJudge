package cn.llonvne.database.resolver

import cn.llonvne.database.repository.GroupRepository
import cn.llonvne.database.resolver.JoinGroupVisibilityCheckResolver.JoinGroupVisibilityCheckResult.*
import cn.llonvne.entity.AuthenticationUser
import cn.llonvne.entity.group.GroupId
import cn.llonvne.entity.role.TeamIdRole
import cn.llonvne.entity.role.TeamMember
import cn.llonvne.kvision.service.GroupIdNotFound
import cn.llonvne.kvision.service.IGroupService
import cn.llonvne.kvision.service.IGroupService.JoinGroupResp
import cn.llonvne.kvision.service.RoleService
import cn.llonvne.security.UserRole
import org.springframework.stereotype.Service

@Service
class JoinGroupResolver(
    private val groupRepository: GroupRepository,
    private val visibilityCheckResolver: JoinGroupVisibilityCheckResolver,
    private val roleService: RoleService
) {
    suspend fun resolve(groupId: GroupId, id: Int, authenticationUser: AuthenticationUser): JoinGroupResp {
        val group = groupRepository.fromId(id) ?: return GroupIdNotFound(groupId)

        return when (visibilityCheckResolver.resolve(
            group.visibility, groupId
        )) {
            Accepted -> accept(authenticationUser, id, groupId)
            Waiting -> waiting()
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

    private suspend fun waiting(): JoinGroupResp {
        TODO()
    }
}