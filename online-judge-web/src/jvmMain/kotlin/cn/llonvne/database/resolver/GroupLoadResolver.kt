package cn.llonvne.database.resolver

import cn.llonvne.database.aware.GroupInfoAwareProvider
import cn.llonvne.database.aware.GroupInfoAwareProvider.GroupInfoAware
import cn.llonvne.database.repository.GroupRepository
import cn.llonvne.entity.AuthenticationUser
import cn.llonvne.entity.group.GroupId
import cn.llonvne.entity.role.*
import cn.llonvne.kvision.service.GroupIdNotFound
import cn.llonvne.kvision.service.IGroupService.LoadGroupResp
import cn.llonvne.kvision.service.IGroupService.LoadGroupResp.*
import org.springframework.stereotype.Service

@Service
class GroupLoadResolver(
    private val groupRepository: GroupRepository,
    private val groupRoleResolver: GroupRoleResolver,
    private val guestPasser: GroupGuestPassResolver,
    private val groupInfoAwareProvider: GroupInfoAwareProvider
) {

    suspend fun resolve(
        originGroupId: GroupId,
        groupId: Int,
        authenticationUser: AuthenticationUser?
    ): LoadGroupResp {

        val group = groupRepository.fromId(groupId) ?: return GroupIdNotFound(originGroupId)

        return groupInfoAwareProvider.awareOf(originGroupId, groupId, group) {
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
                groupId = groupId,
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
        return ManagerLoadGroup(
            groupId = groupId,
            groupName = group.groupName,
            groupShortName = group.groupShortName,
            visibility = group.visibility,
            type = group.type,
            ownerName = ownerName(),
            members = memberOfManager(),
            description = group.description,
            createAt = group.createdAt!!
        )
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