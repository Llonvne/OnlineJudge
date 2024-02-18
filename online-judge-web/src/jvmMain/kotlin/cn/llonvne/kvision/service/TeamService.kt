package cn.llonvne.kvision.service

import cn.llonvne.entity.group.GroupType.*
import cn.llonvne.entity.role.CreateTeam
import cn.llonvne.kvision.service.ITeamService.*
import cn.llonvne.security.AuthenticationToken
import cn.llonvne.security.RedisAuthenticationService
import cn.llonvne.security.RedisAuthenticationService.UserValidatorDsl
import cn.llonvne.security.check
import org.springframework.beans.factory.config.ConfigurableBeanFactory
import org.springframework.context.annotation.EnableAspectJAutoProxy
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import kotlin.reflect.KClass

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