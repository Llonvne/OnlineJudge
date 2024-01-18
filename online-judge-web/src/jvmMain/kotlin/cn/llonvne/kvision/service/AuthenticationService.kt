package cn.llonvne.kvision.service

import cn.llonvne.database.service.AuthenticationUserRepository
import cn.llonvne.message.Message
import cn.llonvne.message.MessageLevel
import cn.llonvne.security.AuthenticationToken
import org.komapper.core.dsl.QueryDsl
import org.komapper.core.dsl.query.Query
import org.springframework.beans.factory.config.ConfigurableBeanFactory
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@Suppress("ACTUAL_WITHOUT_EXPECT")
actual class AuthenticationService(
    private val authenticationUserRepository: AuthenticationUserRepository
) : IAuthenticationService {


    private val bannedUsernameKeyWords = listOf("admin")

    override suspend fun register(username: String, password: String): IAuthenticationService.RegisterResult {

        bannedUsernameKeyWords.forEach { bannedKeyWord ->
            if (username.lowercase().contains(bannedKeyWord)) {
                return IAuthenticationService.RegisterResult.Failed(
                    Message.ToastMessage(
                        MessageLevel.Warning, "用户名不得包含 $bannedKeyWord"
                    )
                )
            }
        }

        // 检查用户名是否可用
        return if (authenticationUserRepository.usernameAvailable(username)) {

            // 返回成功注册
            IAuthenticationService.RegisterResult.SuccessfulRegistration(
                AuthenticationToken(username, authenticationUserRepository.new(username, password).encryptedPassword)
            )

        } else {
            // 提示用户名已经存在
            IAuthenticationService.RegisterResult.Failed(
                Message.ToastMessage(
                    MessageLevel.Warning, "用户名：$username 已经存在"
                )
            )
        }
    }

    override suspend fun login(username: String, password: String): IAuthenticationService.LoginResult {
        return authenticationUserRepository.login(username, password)
    }
}