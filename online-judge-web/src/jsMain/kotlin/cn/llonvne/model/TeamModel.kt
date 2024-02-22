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
import cn.llonvne.security.AuthenticationToken
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

    fun GroupLoader.Companion.of(groupId: GroupId): GroupLoader {
        return GroupLoader {
            load(groupId)
        }
    }

    suspend fun load(groupId: GroupId): LoadGroupResp {
        return teamService.load(AuthenticationModel.userToken.value, groupId)
    }

    suspend fun join(authenticationToken: AuthenticationToken, groupId: GroupId): JoinGroupResp {
        return teamService.join(groupId, authenticationToken)
    }

    suspend fun quit(groupId: GroupId): QuitGroupResp {
        return teamService.quit(groupId, AuthenticationModel.userToken.value)
    }

    suspend fun kick(groupId: GroupId, kickMemberId: Int): KickGroupResp {
        return teamService.kick(AuthenticationModel.userToken.value, groupId, kickMemberId)
    }

    suspend fun upgradeGroupManger(groupId: GroupId, userId: Int): UpgradeGroupManagerResp {
        return teamService.upgradeGroupManager(AuthenticationModel.userToken.value, groupId, userId)
    }

    suspend fun downgradeToMember(groupId: GroupId, userId: Int): DowngradeToMemberResp {
        return teamService.downgradeToMember(AuthenticationModel.userToken.value, groupId, userId)
    }
}