package cn.llonvne.model

import cn.llonvne.kvision.service.IAuthenticationService
import cn.llonvne.message.Messager
import cn.llonvne.security.AuthenticationToken
import io.kvision.remote.getService
import io.kvision.state.ObservableValue
import kotlinx.browser.localStorage
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

object AuthenticationModel {
    private val authenticationService = getService<IAuthenticationService>()
    var userToken: ObservableValue<AuthenticationToken?> = ObservableValue(null)

    init {
        restore()
    }


    suspend fun register(username: String, password: String) = authenticationService.register(username, password)

    suspend fun login(username: String, password: String): IAuthenticationService.LoginResult {
        val result = authenticationService.login(username, password)

        when (result) {
            is IAuthenticationService.LoginResult.SuccessfulLogin -> {
                userToken.value = result.token
                save()
            }

            else -> {}
        }

        return result
    }

    fun logout() {
        userToken.value = null
    }

    private const val authenticationKey = "authentication-key"

    private fun save() {
        localStorage.setItem(
            authenticationKey,
            Json.encodeToString(userToken.value)
        )
    }

    private fun clear() {
        localStorage.removeItem(authenticationKey)
    }

    private fun restore() {
        val tokenStr = localStorage.getItem(authenticationKey)
        if (tokenStr != null) {
            userToken.value = Json.decodeFromString<AuthenticationToken?>(tokenStr)
                .also {
                    if (it != null) {
                        Messager.toastInfo("欢迎回来，${it.username}")
                    }
                }
        }
    }
}