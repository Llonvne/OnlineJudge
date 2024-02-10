package cn.llonvne.model

import cn.llonvne.AppScope
import cn.llonvne.kvision.service.IAuthenticationService
import cn.llonvne.kvision.service.IAuthenticationService.GetLoginInfoResp.*
import cn.llonvne.kvision.service.IAuthenticationService.GetLogoutResp.Logout
import cn.llonvne.kvision.service.IAuthenticationService.LoginResult
import cn.llonvne.kvision.service.IAuthenticationService.LoginResult.SuccessfulLogin
import cn.llonvne.message.Messager
import cn.llonvne.security.AuthenticationToken
import io.kvision.remote.getService
import io.kvision.state.ObservableValue
import kotlinx.browser.localStorage
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

object AuthenticationModel {
    private val authenticationService = getService<IAuthenticationService>()
    var userToken: ObservableValue<AuthenticationToken?> = ObservableValue(null)

    init {
        restore()
    }


    suspend fun register(username: String, password: String) = authenticationService.register(username, password)

    suspend fun login(username: String, password: String): LoginResult {
        val result = authenticationService.login(username, password)

        when (result) {
            is SuccessfulLogin -> {
                userToken.value = result.token
                save()
            }

            else -> {}
        }

        return result
    }

    fun logout() {
        AppScope.launch {
            when (authenticationService.logout(userToken.value)) {
                Logout -> {
                    Messager.toastInfo("登出成功")
                }
            }
            clear()
            userToken.value = null
        }
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

            AppScope.launch {
                val info = info()
                if (info == null) {
                    userToken.value = null
                } else {
                    Messager.toastInfo("欢迎回来，${info.username}")
                }
            }
        }
    }

    suspend fun info(): Login? {
        return when (val resp = authenticationService.getLoginInfo(this.userToken.value)) {
            is Login -> resp
            LoginExpired -> null
            NotLogin -> null
        }
    }
}