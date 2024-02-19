package cn.llonvne.database.resolver

import cn.llonvne.database.repository.GroupRepository
import cn.llonvne.entity.AuthenticationUser
import cn.llonvne.entity.group.Group
import cn.llonvne.entity.group.GroupId.IntGroupId
import cn.llonvne.entity.role.*
import cn.llonvne.kvision.service.GroupIdNotFound
import cn.llonvne.kvision.service.IGroupService.LoadGroupResp
import org.springframework.stereotype.Service

@Service
class GroupLoadResolver(
    private val groupRepository: GroupRepository,
    private val groupRoleResolver: GroupRoleResolver,
    private val groupIdToUserResolver: GroupIdToUserResolver
) {

    inner class GroupInfoAware(
        val id: Int,
        val group: Group
    ) {

        suspend fun ownerName(): String {
            return groupIdToUserResolver.resolve(GroupManager.GroupMangerImpl(id)).firstOrNull()?.username ?: "<未找到>"
        }
    }

    suspend fun <R> awareOf(id: Int, group: Group, action: suspend GroupInfoAware.() -> R): R {
        return GroupInfoAware(id, group).action()
    }

    suspend fun resolve(
        groupId: Int,
        authenticationUser: AuthenticationUser?
    ): LoadGroupResp {

        val group = groupRepository.fromId(groupId) ?: return GroupIdNotFound(IntGroupId(groupId))

        return awareOf(groupId, group) {
            if (authenticationUser == null) {
                return@awareOf loadAsGuest()
            }

            val teamRole = groupRoleResolver.resolve(groupId, authenticationUser) ?: return@awareOf loadAsGuest()

            return@awareOf when (teamRole) {
                is DeleteTeam.DeleteTeamImpl -> loadAsGuest()
                is GroupManager.GroupMangerImpl -> loadAsGroupManager()
                is InviteMember.InviteMemberImpl -> loadAsGuest()
                is KickMember.KickMemberImpl -> loadAsGuest()
                is TeamMember.TeamMemberImpl -> loadAsMember()
                TeamSuperManager.TeamSuperManagerImpl -> loadAsSuperManager()
            }
        }
    }

    context(GroupInfoAware)
    suspend fun loadAsGuest(): LoadGroupResp {
        return LoadGroupResp.GuestLoadGroup(
            groupName = group.groupName,
            groupShortName = group.groupShortName,
            visibility = group.visibility,
            type = group.type,
            ownerName = ownerName()
        )
    }

    context(GroupInfoAware)
    suspend fun loadAsGroupManager(): LoadGroupResp {
        TODO()
    }

    context(GroupInfoAware)
    suspend fun loadAsMember(): LoadGroupResp {
        TODO()
    }

    context(GroupInfoAware)
    suspend fun loadAsSuperManager(): LoadGroupResp {
        TODO()
    }
}