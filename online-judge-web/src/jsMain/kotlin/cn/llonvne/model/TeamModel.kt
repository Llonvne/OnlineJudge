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
}