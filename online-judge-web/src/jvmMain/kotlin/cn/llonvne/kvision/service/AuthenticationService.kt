package cn.llonvne.kvision.service

import cn.llonvne.database.repository.AuthenticationUserRepository
import cn.llonvne.database.resolver.authentication.BannedUsernameCheckResolver
import cn.llonvne.database.resolver.authentication.BannedUsernameCheckResolver.BannedUsernameCheckResult
import cn.llonvne.database.resolver.authentication.BannedUsernameCheckResolver.BannedUsernameCheckResult.Pass
import cn.llonvne.database.resolver.mine.MineRoleCheckResolver
import cn.llonvne.kvision.service.IAuthenticationService.*
import cn.llonvne.kvision.service.IAuthenticationService.GetLoginInfoResp.*
import cn.llonvne.kvision.service.IAuthenticationService.GetLogoutResp.Logout
import cn.llonvne.kvision.service.IAuthenticationService.RegisterResult.Failed
import cn.llonvne.kvision.service.IAuthenticationService.RegisterResult.SuccessfulRegistration
import cn.llonvne.message.Message
import cn.llonvne.message.MessageLevel
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
actual class AuthenticationService(
    private val userRepository: AuthenticationUserRepository,
    private val authentication: RedisAuthenticationService,
    private val bannedUsernameCheckResolver: BannedUsernameCheckResolver,
    private val mineResolver: MineRoleCheckResolver
) : IAuthenticationService {
    override suspend fun register(username: String, password: String): RegisterResult {

        when (bannedUsernameCheckResolver.resolve(username)) {
            Pass -> {}
            BannedUsernameCheckResult.Failed -> return Failed(
                Message.ToastMessage(MessageLevel.Danger, "名称包含违禁词")
            )
        }

        // 检查用户名是否可用
        return if (userRepository.usernameAvailable(username)) {
            val user = userRepository.new(username, password)
            // 返回成功注册
            SuccessfulRegistration(authentication.login(user), username)

        } else {
            // 提示用户名已经存在
            Failed(Message.ToastMessage(MessageLevel.Warning, "用户名：$username 已经存在"))
        }
    }

    override suspend fun login(username: String, password: String): LoginResult {
        return userRepository.login(username, password)
    }

    override suspend fun getLoginInfo(token: AuthenticationToken?): GetLoginInfoResp {
        if (token == null) {
            return NotLogin
        }
        val user = authentication.validate(token) { requireLogin() }

        if (user == null) {
            return LoginExpired
        }

        return Login(user.username, user.id)
    }

    override suspend fun logout(token: AuthenticationToken?): GetLogoutResp {
        authentication.logout(token)
        return Logout
    }

    override suspend fun mine(value: AuthenticationToken?): MineResp {
        val user = authentication.validate(value) {
            requireLogin()
        } ?: return PermissionDenied
        return mineResolver.resolve(user)
    }
}