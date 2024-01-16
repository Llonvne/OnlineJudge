package cn.llonvne.kvision.service

import cn.llonvne.database.service.AuthenticationUserRepository
import cn.llonvne.security.AuthenticationToken
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
    override suspend fun register(username: String, password: String): IAuthenticationService.RegisterResult {
        return if (authenticationUserRepository.usernameAvailable(username)) {
            IAuthenticationService.RegisterResult.SuccessfulRegistration(
                AuthenticationToken(authenticationUserRepository.new(username, password).encryptedPassword)
            )
        } else {
            IAuthenticationService.RegisterResult.Failed("username:$username 已经存在")
        }
    }

    override suspend fun login(username: String, password: String): Boolean {
        return authenticationUserRepository.login(username, password)
    }
}