package cn.llonvne.kvision.service

import cn.llonvne.entity.group.Group
import cn.llonvne.entity.group.GroupId
import cn.llonvne.entity.group.GroupType
import cn.llonvne.entity.group.GroupVisibility
import cn.llonvne.entity.role.TeamIdRole
import cn.llonvne.kvision.service.Validatable.Companion.on
import cn.llonvne.kvision.service.Validatable.Companion.validate
import cn.llonvne.security.AuthenticationToken
import io.kvision.annotations.KVService
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@KVService
interface IGroupService {
    @Serializable
    data class CreateGroupReq(
        val groupName: String,
        val groupShortName: String,
        val teamVisibility: GroupVisibility,
        val groupType: GroupType,
        val description: String
    ) : Validatable {
        override fun validate() = validate {
            on(groupName, "队伍名称必须在 6..100 之间") {
                length in 6..100
            }

            on(groupShortName, "短名称必须在 3..20之间") {
                length in 3..20
            }
        }
    }

    @Serializable
    sealed interface CreateGroupResp {
        @Serializable
        data class CreateGroupOk(val group: Group) : CreateGroupResp
    }

    suspend fun createTeam(
        authenticationToken: AuthenticationToken, createGroupReq: CreateGroupReq
    ): CreateGroupResp

    @Serializable
    sealed interface LoadGroupResp {
        sealed interface LoadGroupSuccessResp : LoadGroupResp {
            val groupId: GroupId
            val groupName: String
            val groupShortName: String
            val visibility: GroupVisibility

            @SerialName("groupType")
            val type: GroupType
            val ownerName: String
            val description: String
            val members: List<GroupMemberDto>
            val createAt: LocalDateTime
        }

        @Serializable
        data class GuestLoadGroup(
            override val groupId: GroupId,
            override val groupName: String,
            override val groupShortName: String,
            override val visibility: GroupVisibility,
            @SerialName("groupType")
            override val type: GroupType,
            override val ownerName: String,
            override val description: String,
            override val members: List<GroupMemberDtoImpl>,
            override val createAt: LocalDateTime,
        ) : LoadGroupSuccessResp

        @Serializable
        data class ManagerLoadGroup(
            override val groupId: GroupId,
            override val groupName: String,
            override val groupShortName: String,
            override val visibility: GroupVisibility,
            @SerialName("groupType") override val type: GroupType,
            override val ownerName: String,
            override val description: String,
            override val members: List<GroupMemberDtoImpl>,
            override val createAt: LocalDateTime
        ) : LoadGroupSuccessResp

        @Serializable
        data class MemberLoadGroup(
            override val groupId: GroupId,
            override val groupName: String,
            override val groupShortName: String,
            override val visibility: GroupVisibility,
            @SerialName("groupType") override val type: GroupType,
            override val ownerName: String,
            override val description: String,
            override val members: List<GroupMemberDtoImpl>,
            override val createAt: LocalDateTime
        ) : LoadGroupSuccessResp

        @Serializable
        data class OwnerLoadGroup(
            override val groupId: GroupId,
            override val groupName: String,
            override val groupShortName: String,
            override val visibility: GroupVisibility,
            @SerialName("groupType") override val type: GroupType,
            override val ownerName: String,
            override val description: String,
            override val members: List<GroupMemberDtoImpl>,
            override val createAt: LocalDateTime
        ) : LoadGroupSuccessResp

        @Serializable
        sealed interface GroupMemberDto {
            val username: String
            val role: TeamIdRole
            val userId: Int
        }

        @Serializable
        data class GroupMemberDtoImpl(
            override val username: String, override val role: TeamIdRole,
            override val userId: Int
        ) : GroupMemberDto
    }

    suspend fun load(authenticationToken: AuthenticationToken?, groupId: GroupId): LoadGroupResp

    @Serializable
    sealed interface JoinGroupResp {
        @Serializable
        data class Joined(val groupId: GroupId) : JoinGroupResp

        @Serializable
        data class Reject(val groupId: GroupId) : JoinGroupResp

        @Serializable
        data class NoManagersFound(val groupId: GroupId) : JoinGroupResp
    }

    suspend fun join(groupId: GroupId, authenticationToken: AuthenticationToken): JoinGroupResp

    @Serializable
    sealed interface QuitGroupResp

    @Serializable
    data object QuitOk : QuitGroupResp

    suspend fun quit(groupId: GroupId, value: AuthenticationToken?): QuitGroupResp

    @Serializable
    sealed interface KickGroupResp {
        @Serializable
        data class KickMemberNotFound(val memberId: Int) : KickGroupResp

        @Serializable
        data class KickMemberGroupIdRoleFound(val memberId: Int, val groupId: GroupId) : KickGroupResp

        @Serializable
        data object Kicked : KickGroupResp
    }

    suspend fun kick(token: AuthenticationToken?, groupId: GroupId, kickMemberId: Int): KickGroupResp

    @Serializable
    sealed interface UpgradeGroupManagerResp {
        @Serializable
        data object UpgradeManagerOk : UpgradeGroupManagerResp

        @Serializable
        data class BeManagerNotFound(val userId: Int) : UpgradeGroupManagerResp

        @Serializable
        data class UserAlreadyHasThisRole(val userId: Int) : UpgradeGroupManagerResp

        @Serializable
        data class UpdateToIdNotMatchToGroupId(val userId: Int) : UpgradeGroupManagerResp
    }

    suspend fun upgradeGroupManager(
        token: AuthenticationToken?,
        groupId: GroupId,
        updatee: Int
    ): UpgradeGroupManagerResp
}