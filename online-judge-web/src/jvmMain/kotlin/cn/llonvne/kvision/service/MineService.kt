package cn.llonvne.kvision.service

import cn.llonvne.database.repository.AuthenticationUserRepository
import cn.llonvne.database.resolver.mine.BackendInfoResolver
import cn.llonvne.database.resolver.mine.JudgeServerInfoResolver
import cn.llonvne.database.resolver.mine.OnlineJudgeStatisticsResolver
import cn.llonvne.entity.ModifyUserForm
import cn.llonvne.entity.role.Backend
import cn.llonvne.entity.role.Banned
import cn.llonvne.entity.role.IUserRole
import cn.llonvne.entity.role.check
import cn.llonvne.exts.*
import cn.llonvne.kvision.service.IMineService.DashboardResp.DashboardRespImpl
import cn.llonvne.security.AuthenticationToken
import cn.llonvne.security.RedisAuthenticationService
import cn.llonvne.security.check
import cn.llonvne.security.userRole
import org.springframework.beans.factory.config.ConfigurableBeanFactory
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@Suppress("ACTUAL_WITHOUT_EXPECT")
actual class MineService(
    private val authentication: RedisAuthenticationService,
    private val onlineJudgeStatisticsResolver: OnlineJudgeStatisticsResolver,
    private val backendInfoResolver: BackendInfoResolver,
    private val judgeServerInfoResolver: JudgeServerInfoResolver,
    private val authenticationUserRepository: AuthenticationUserRepository,
    private val roleService: RoleService
) : IMineService {
    override suspend fun dashboard(authenticationToken: AuthenticationToken?): IMineService.DashboardResp {
        val user = authentication.validate(authenticationToken) {
            requireLogin()
            check(Backend.BackendImpl)
        } ?: return PermissionDenied
        return DashboardRespImpl(
            statistics = onlineJudgeStatisticsResolver.resolve(),
            backendInfo = backendInfoResolver.resolve(),
            judgeServerInfo = judgeServerInfoResolver.resolve()
        )
    }

    override suspend fun users(): IMineService.UsersResp {

        val user = authenticationUserRepository.all()
            .map {
                IMineService.UsersResp.UserManageListUserDto(
                    userId = it.id,
                    username = it.username,
                    userRole = IUserRole(it.userRole.roles),
                    createAt = it.createdAt ?: kotlinx.datetime.LocalDateTime.now()
                )
            }
        return IMineService.UsersResp.UsersRespImpl(
            users = user
        )
    }

    override suspend fun deleteUser(value: AuthenticationToken?, id: Int): Boolean {
        return authenticationUserRepository.deleteById(id)
    }

    override suspend fun modifyUser(value: AuthenticationToken?, result: ModifyUserForm): Boolean {
        val id = result.userId.toIntOrNull() ?: return false
        val user = authentication.getAuthenticationUser(id) ?: return false
        if (result.isBanned) {
            if (!user.check(Banned.BannedImpl)) {
                return roleService.addRole(id, Banned.BannedImpl)
            }
        } else {
            return roleService.removeRole(user, listOf(Banned.BannedImpl))
        }
        return false
    }
}