package cn.llonvne.kvision.service

import cn.llonvne.message.Message
import cn.llonvne.message.MessageLevel
import cn.llonvne.security.AuthenticationToken
import io.kvision.annotations.KVService
import kotlinx.serialization.Serializable

@KVService
interface IAuthenticationService {
    @Serializable
    sealed interface RegisterResult {
        val message: Message.ToastMessage

        fun isOk() = this is SuccessfulRegistration

        @Serializable
        data class SuccessfulRegistration(val token: AuthenticationToken) : RegisterResult {
            override val message: Message.ToastMessage = Message.ToastMessage(
                MessageLevel.Success, "成功注册，欢迎新用户:${token.username}"
            )
        }

        @Serializable
        data class Failed(override val message: Message.ToastMessage) : RegisterResult
    }

    suspend fun register(username: String, password: String): RegisterResult

    @Serializable
    sealed interface LoginResult {

        fun isOk() = this is SuccessfulLogin

        val message: Message

        @Serializable
        data class SuccessfulLogin(val token: AuthenticationToken) : LoginResult {
            override val message: Message =
                Message.ToastMessage(MessageLevel.Success, "登入成功，欢迎:${token.username}")
        }

        @Serializable
        data object IncorrectUsernameOrPassword : LoginResult {
            override val message: Message = Message.ToastMessage(MessageLevel.Warning, "用户名或者密码错误")
        }

        @Serializable
        data object UserDoNotExist : LoginResult {
            override val message: Message = Message.ToastMessage(MessageLevel.Warning, "用户不存在")
        }
    }

    suspend fun login(username: String, password: String): LoginResult
}