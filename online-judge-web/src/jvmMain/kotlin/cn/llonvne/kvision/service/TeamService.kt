package cn.llonvne.kvision.service

import cn.llonvne.entity.role.CreateTeam
import cn.llonvne.kvision.service.ITeamService.CreateTeamReq
import cn.llonvne.kvision.service.ITeamService.CreateTeamResp
import cn.llonvne.security.AuthenticationToken
import cn.llonvne.security.RedisAuthenticationService
import org.springframework.beans.factory.config.ConfigurableBeanFactory
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@Suppress("ACTUAL_WITHOUT_EXPECT")
actual class TeamService(
    val authentication: RedisAuthenticationService
) : ITeamService {
    override suspend fun createTeam(
        authenticationToken: AuthenticationToken,
        createTeamReq: CreateTeamReq
    ): CreateTeamResp {
        val user = authentication.validate(authenticationToken) {
            check(CreateTeam.require(createTeamReq.teamType))
        } ?: return PermissionDenied
        TODO()
    }
}