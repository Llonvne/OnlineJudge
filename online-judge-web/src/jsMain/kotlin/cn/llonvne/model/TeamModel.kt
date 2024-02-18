package cn.llonvne.model

import cn.llonvne.kvision.service.ITeamService
import cn.llonvne.kvision.service.PermissionDenied
import io.kvision.remote.getService

object TeamModel {
    private val teamService = getService<ITeamService>()

    suspend fun create(create: ITeamService.CreateTeamReq): ITeamService.CreateTeamResp {
        val token = AuthenticationModel.userToken.value ?: return PermissionDenied
        return teamService.createTeam(token, create)
    }
}