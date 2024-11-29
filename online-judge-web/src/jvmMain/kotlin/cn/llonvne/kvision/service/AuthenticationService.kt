package cn.llonvne.kvision.service

import cn.llonvne.database.repository.UserRepository
import cn.llonvne.database.resolver.authentication.BannedUsernameCheckResolver
import cn.llonvne.database.resolver.authentication.BannedUsernameCheckResolver.BannedUsernameCheckResult
import cn.llonvne.database.resolver.authentication.BannedUsernameCheckResolver.BannedUsernameCheckResult.Pass
import cn.llonvne.database.resolver.mine.MineLoadAsNormalUserOrBackendDeterminer
import cn.llonvne.kvision.service.IAuthenticationService.*
import cn.llonvne.kvision.service.IAuthenticationService.LoginInfoResp.*
import cn.llonvne.kvision.service.IAuthenticationService.LogoutResp.Logout
import cn.llonvne.kvision.service.IAuthenticationService.RegisterResp.Failed
import cn.llonvne.kvision.service.IAuthenticationService.RegisterResp.Successful
import cn.llonvne.message.Message
import cn.llonvne.message.MessageLevel
import cn.llonvne.security.LoginLogoutResolver
import cn.llonvne.security.Token
import cn.llonvne.security.TokenValidator
import org.springframework.beans.factory.config.ConfigurableBeanFactory
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@Suppress("ACTUAL_WITHOUT_EXPECT")
actual class AuthenticationService(
    private val userRepository: UserRepository,
    private val bannedUsernameCheckResolver: BannedUsernameCheckResolver,
    private val mineLoadAsNormalUserOrBackendDeterminer: MineLoadAsNormalUserOrBackendDeterminer,
    private val tokenValidator: TokenValidator,
    private val loginLogoutResolver: LoginLogoutResolver,
) : IAuthenticationService {
    override suspend fun register(
        username: String,
        password: String,
    ): RegisterResp {
        when (bannedUsernameCheckResolver.resolve(username)) {
            Pass -> {}
            BannedUsernameCheckResult.Failed -> return Failed(
                Message.ToastMessage(MessageLevel.Danger, "名称包含违禁词"),
            )
        }

        // 检查用户名是否可用
        return if (userRepository.usernameAvailable(username)) {
            val user = userRepository.new(username, password)
            // 返回成功注册
            Successful(loginLogoutResolver.login(user), username)
        } else {
            // 提示用户名已经存在
            Failed(Message.ToastMessage(MessageLevel.Warning, "用户名：$username 已经存在"))
        }
    }

    override suspend fun login(
        username: String,
        password: String,
    ): LoginResp = userRepository.login(username, password)

    override suspend fun loginInfo(token: Token?): LoginInfoResp {
        if (token == null) {
            return NotLogin
        }
        val user = tokenValidator.validate(token) { requireLogin() }

        if (user == null) {
            return LoginExpired
        }

        return Logined(user.username, user.id)
    }

    override suspend fun logout(token: Token?): LogoutResp {
        loginLogoutResolver.logout(token)
        return Logout
    }

    override suspend fun mine(token: Token?): MineResp {
        val user =
            tokenValidator.validate(token) {
                requireLogin()
            } ?: return PermissionDenied
        return mineLoadAsNormalUserOrBackendDeterminer.resolve(user)
    }
}
