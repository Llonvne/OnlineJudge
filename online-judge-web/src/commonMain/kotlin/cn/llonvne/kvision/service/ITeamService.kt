package cn.llonvne.kvision.service

import cn.llonvne.entity.group.GroupType
import cn.llonvne.security.AuthenticationToken
import io.kvision.annotations.KVService
import kotlinx.serialization.Serializable

@KVService
interface ITeamService {
    @Serializable
    data class CreateTeamReq(
        val id: Nothing,
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