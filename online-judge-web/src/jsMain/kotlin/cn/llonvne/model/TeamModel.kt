package cn.llonvne.model

import cn.llonvne.entity.group.GroupId
import cn.llonvne.entity.group.GroupLoader
import cn.llonvne.kvision.service.ClientError
import cn.llonvne.kvision.service.IGroupService
import cn.llonvne.kvision.service.IGroupService.*
import cn.llonvne.kvision.service.PermissionDenied
import cn.llonvne.kvision.service.Validatable.Companion.Failed
import cn.llonvne.kvision.service.Validatable.Companion.Ok
import cn.llonvne.message.Messager
import cn.llonvne.security.Token
import io.kvision.remote.getService

object TeamModel {
    private val teamService = getService<IGroupService>()

    suspend fun create(create: CreateGroupReq): CreateGroupResp {
        val token = AuthenticationModel.userToken.value ?: return PermissionDenied

        when (val result = create.validate()) {
            Ok -> return teamService.createTeam(token, create)
            is Failed -> {
                Messager.toastInfo(result.message)
                return ClientError(result.message)
            }
        }
    }

    fun GroupLoader.Companion.of(groupId: GroupId): GroupLoader =
        GroupLoader {
            load(groupId)
        }

    suspend fun load(groupId: GroupId): LoadGroupResp = teamService.load(AuthenticationModel.userToken.value, groupId)

    suspend fun join(
        token: Token,
        groupId: GroupId,
    ): JoinGroupResp = teamService.join(groupId, token)

    suspend fun quit(groupId: GroupId): QuitGroupResp = teamService.quit(groupId, AuthenticationModel.userToken.value)

    suspend fun kick(
        groupId: GroupId,
        kickMemberId: Int,
    ): KickGroupResp = teamService.kick(AuthenticationModel.userToken.value, groupId, kickMemberId)

    suspend fun upgradeGroupManger(
        groupId: GroupId,
        userId: Int,
    ): UpgradeGroupManagerResp = teamService.upgradeGroupManager(AuthenticationModel.userToken.value, groupId, userId)

    suspend fun downgradeToMember(
        groupId: GroupId,
        userId: Int,
    ): DowngradeToMemberResp = teamService.downgradeToMember(AuthenticationModel.userToken.value, groupId, userId)
}
