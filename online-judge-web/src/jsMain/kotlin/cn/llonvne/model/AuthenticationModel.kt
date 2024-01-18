package cn.llonvne.model

import cn.llonvne.kvision.service.IAuthenticationService
import cn.llonvne.security.AuthenticationToken
import io.kvision.remote.getService
import io.kvision.state.ObservableValue

object AuthenticationModel {
    private val authenticationService = getService<IAuthenticationService>()

    var userToken: ObservableValue<AuthenticationToken?> = ObservableValue(null)
    suspend fun register(username: String, password: String) = authenticationService.register(username, password)

    suspend fun login(username: String, password: String): IAuthenticationService.LoginResult {
        val result = authenticationService.login(username, password)

        when (result) {
            is IAuthenticationService.LoginResult.SuccessfulLogin -> {
                userToken.value = result.token
            }

            else -> {}
        }

        return result
    }

    fun logout() {
        userToken.value = null
    }
}