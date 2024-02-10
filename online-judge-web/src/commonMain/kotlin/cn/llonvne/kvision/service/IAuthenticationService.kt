package cn.llonvne.kvision.service

import cn.llonvne.message.Message
import cn.llonvne.message.Message.ToastMessage
import cn.llonvne.message.MessageLevel
import cn.llonvne.security.AuthenticationToken
import io.kvision.annotations.KVService
import kotlinx.serialization.Serializable

@KVService
interface IAuthenticationService {
    @Serializable
    sealed interface RegisterResult {
        val message: ToastMessage

        fun isOk() = this is SuccessfulRegistration

        @Serializable
        data class SuccessfulRegistration(val token: AuthenticationToken, val username: String) : RegisterResult {
            override val message: ToastMessage = ToastMessage(
                MessageLevel.Success, "成功注册，欢迎新用户:${username}"
            )
        }

        @Serializable
        data class Failed(override val message: ToastMessage) : RegisterResult
    }

    suspend fun register(username: String, password: String): RegisterResult

    @Serializable
    sealed interface LoginResult {

        fun isOk() = this is SuccessfulLogin

        val message: Message

        @Serializable
        data class SuccessfulLogin(val token: AuthenticationToken, val username: String) : LoginResult {
            override val message: Message =
                ToastMessage(MessageLevel.Success, "登入成功，欢迎:${username}")
        }

        @Serializable
        data object IncorrectUsernameOrPassword : LoginResult {
            override val message: Message = ToastMessage(MessageLevel.Warning, "用户名或者密码错误")
        }

        @Serializable
        data object UserDoNotExist : LoginResult {
            override val message: Message = ToastMessage(MessageLevel.Warning, "用户不存在")
        }
    }

    suspend fun login(username: String, password: String): LoginResult

    @Serializable
    sealed interface GetLoginInfoResp {
        @Serializable
        data object NotLogin : GetLoginInfoResp

        @Serializable
        data object LoginExpired : GetLoginInfoResp

        @Serializable
        data class Login(val username: String, val id: Int) : GetLoginInfoResp
    }

    suspend fun getLoginInfo(token: AuthenticationToken?): GetLoginInfoResp

    @Serializable
    sealed interface GetLogoutResp {
        @Serializable
        data object Logout : GetLogoutResp
    }

    suspend fun logout(token: AuthenticationToken?):GetLogoutResp
}