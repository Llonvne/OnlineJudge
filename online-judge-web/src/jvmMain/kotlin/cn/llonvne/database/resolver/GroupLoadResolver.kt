package cn.llonvne.database.resolver

import cn.llonvne.database.repository.GroupRepository
import cn.llonvne.entity.AuthenticationUser
import cn.llonvne.entity.group.Group
import cn.llonvne.entity.group.GroupId
import cn.llonvne.entity.group.GroupId.IntGroupId
import cn.llonvne.entity.role.*
import cn.llonvne.kvision.service.GroupIdNotFound
import cn.llonvne.kvision.service.IGroupService.LoadGroupResp
import cn.llonvne.kvision.service.IGroupService.LoadGroupResp.GuestLoadGroup
import cn.llonvne.kvision.service.IGroupService.LoadGroupResp.GuestLoadGroup.GroupMemberDto
import org.springframework.stereotype.Service

@Service
class GroupLoadResolver(
    private val groupRepository: GroupRepository,
    private val groupRoleResolver: GroupRoleResolver,
    private val groupMembersResolver: GroupMembersResolver,
    private val guestPasser: GroupGuestPassResolver
) {

    inner class GroupInfoAware(
        val groupId: GroupId,
        val id: Int,
        val group: Group
    ) {

        suspend fun ownerName(): String {
            return groupMembersResolver.fromRole(GroupManager.GroupMangerImpl(id)).firstOrNull()?.username ?: "<未找到>"
        }

        suspend fun membersOfGuest(): List<GroupMemberDto> {
            return groupMembersResolver.fromGroupId(id).mapNotNull {
                GroupMemberDto(
                    username = it.username,
                    role = groupRoleResolver.resolve(id, it) ?: return@mapNotNull null
                )
            }
        }
    }

    suspend fun <R> awareOf(groupId: GroupId, id: Int, group: Group, action: suspend GroupInfoAware.() -> R): R {
        return GroupInfoAware(groupId, id, group).action()
    }

    suspend fun resolve(
        originGroupId: GroupId,
        groupId: Int,
        authenticationUser: AuthenticationUser?
    ): LoadGroupResp {

        val group = groupRepository.fromId(groupId) ?: return GroupIdNotFound(IntGroupId(groupId))

        return awareOf(originGroupId, groupId, group) {
            if (authenticationUser == null) {
                return@awareOf loadAsGuestOrReject()
            }

            val teamRole = groupRoleResolver.resolve(groupId, authenticationUser)
                ?: return@awareOf loadAsGuestOrReject()

            return@awareOf when (teamRole) {
                is DeleteTeam.DeleteTeamImpl -> loadAsGuestOrReject()
                is GroupManager.GroupMangerImpl -> loadAsGroupManager()
                is InviteMember.InviteMemberImpl -> loadAsGuestOrReject()
                is KickMember.KickMemberImpl -> loadAsGuestOrReject()
                is TeamMember.TeamMemberImpl -> loadAsMember()
                is TeamSuperManager -> loadAsSuperManager()
            }
        }
    }

    context(GroupInfoAware)
    suspend fun loadAsGuestOrReject(): LoadGroupResp {
        return guestPasser.resolve {
            return@resolve GuestLoadGroup(
                groupName = group.groupName,
                groupShortName = group.groupShortName,
                visibility = group.visibility,
                type = group.type,
                ownerName = ownerName(),
                members = membersOfGuest(),
                description = group.description,
                createAt = group.createdAt!!
            )
        }
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