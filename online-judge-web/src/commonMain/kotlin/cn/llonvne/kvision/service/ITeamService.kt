package cn.llonvne.kvision.service

import cn.llonvne.entity.group.GroupType
import cn.llonvne.entity.group.GroupVisibility
import cn.llonvne.security.AuthenticationToken
import io.kvision.annotations.KVService
import kotlinx.serialization.Serializable

@KVService
interface ITeamService {
    @Serializable
    data class CreateTeamReq(
        val teamName: String,
        val teamShortName: String,
        val teamVisibility: GroupVisibility,
        val teamType: GroupType
    )

    @Serializable
    sealed interface CreateTeamResp {

    }

    suspend fun createTeam(
        authenticationToken: AuthenticationToken,
        createTeamReq: CreateTeamReq
    ): CreateTeamResp
}