package cn.llonvne.model

import cn.llonvne.AppScope
import cn.llonvne.kvision.service.IAuthenticationService
import cn.llonvne.kvision.service.IAuthenticationService.LoginInfoResp.*
import cn.llonvne.kvision.service.IAuthenticationService.LogoutResp.Logout
import cn.llonvne.kvision.service.IAuthenticationService.LoginResp
import cn.llonvne.kvision.service.IAuthenticationService.LoginResp.Successful
import cn.llonvne.message.Messager
import cn.llonvne.security.Token
import io.kvision.remote.getService
import io.kvision.state.ObservableValue
import kotlinx.browser.localStorage
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

object AuthenticationModel {
    private val authenticationService = getService<IAuthenticationService>()
    var userToken: ObservableValue<Token?> = ObservableValue(null)

    init {
        restore()
    }


    suspend fun register(username: String, password: String) = authenticationService.register(username, password)

    suspend fun login(username: String, password: String): LoginResp {
        val result = authenticationService.login(username, password)

        when (result) {
            is Successful -> {
                userToken.value = result.token
                save()
            }

            LoginResp.IncorrectUsernameOrPassword -> {
                Messager.toastInfo("用户名或密码错误")
            }

            LoginResp.UserNotExist -> {
                Messager.toastInfo("用户不存在")
            }

            LoginResp.Banned -> Messager.send(result.message)
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
        }
        userToken.value = null
        clear()
        restore()
    }

    private const val key = "authentication-key"

    private fun save() {
        localStorage.setItem(
            key, Json.encodeToString(userToken.value)
        )
    }

    private fun clear() {
        localStorage.setItem(key, "")
    }

    private fun restore() {
        val tokenStr = localStorage.getItem(key)
        if (tokenStr.isNullOrEmpty()) {
            return
        }
        userToken.value = Json.decodeFromString<Token?>(tokenStr)

        AppScope.launch {
            info()?.let { Messager.toastInfo("欢迎回来，${it.username}") }
                ?: run { userToken.value = null }
        }
    }


    suspend fun info(): Logined? {
        return when (val resp = authenticationService.loginInfo(this.userToken.value)) {
            is Logined -> resp
            LoginExpired -> null
            NotLogin -> null
        }
    }

    suspend fun mine() = authenticationService.mine(userToken.value)
}