package cn.llonvne.database.resolver

import cn.llonvne.entity.AuthenticationUser
import cn.llonvne.entity.role.DeleteTeam.DeleteTeamImpl
import cn.llonvne.entity.role.GroupManager.GroupMangerImpl
import cn.llonvne.entity.role.InviteMember.InviteMemberImpl
import cn.llonvne.entity.role.KickMember.KickMemberImpl
import cn.llonvne.entity.role.TeamIdRole
import cn.llonvne.entity.role.TeamMember.TeamMemberImpl
import cn.llonvne.entity.role.TeamSuperManager
import cn.llonvne.kvision.service.RoleService
import org.springframework.stereotype.Service

@Service
class GroupRoleResolver(
    private val roleService: RoleService,
) {

    private val highest: (TeamIdRole) -> Int = {
        when (it) {
            is DeleteTeamImpl -> 0
            is GroupMangerImpl -> 100
            is InviteMemberImpl -> 0
            is KickMemberImpl -> 0
            is TeamMemberImpl -> 50
            is TeamSuperManager -> 1000
        }
    }

    suspend fun resolve(groupId: Int, authenticationUser: AuthenticationUser): TeamIdRole? {
        val role = roleService.get(authenticationUser.id)?.roles ?: return null
        return role.filterIsInstance<TeamIdRole>()
            .filter { it.teamId == groupId || it is TeamSuperManager }
            .sortedByDescending(highest).firstOrNull()
    }
}