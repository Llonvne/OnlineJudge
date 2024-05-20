package cn.llonvne.kvision.service

import cn.llonvne.message.Message
import cn.llonvne.message.Message.ToastMessage
import cn.llonvne.message.MessageLevel
import cn.llonvne.security.Token
import io.kvision.annotations.KVService
import kotlinx.serialization.Serializable

@KVService
interface IAuthenticationService {
    @Serializable
    sealed interface RegisterResp {
        val message: ToastMessage

        fun isOk() = this is Successful

        @Serializable
        data class Successful(val token: Token, val username: String) : RegisterResp {
            override val message: ToastMessage = ToastMessage(
                MessageLevel.Success, "成功注册，欢迎新用户:${username}"
            )
        }

        @Serializable
        data class Failed(override val message: ToastMessage) : RegisterResp
    }

    suspend fun register(username: String, password: String): RegisterResp

    @Serializable
    sealed interface LoginResp {
        val message: Message

        @Serializable
        data object Banned : LoginResp {
            override val message: Message
                get() = ToastMessage(MessageLevel.Danger, "您的账号已经被管理员封禁")
        }

        @Serializable
        data class Successful(val token: Token, val username: String) : LoginResp {
            override val message: Message =
                ToastMessage(MessageLevel.Success, "登入成功，欢迎:${username}")
        }

        @Serializable
        data object IncorrectUsernameOrPassword : LoginResp {
            override val message: Message = ToastMessage(MessageLevel.Warning, "用户名或者密码错误")
        }

        @Serializable
        data object UserNotExist : LoginResp {
            override val message: Message = ToastMessage(MessageLevel.Warning, "用户不存在")
        }
    }

    suspend fun login(username: String, password: String): LoginResp

    @Serializable
    sealed interface LoginInfoResp {
        @Serializable
        data object NotLogin : LoginInfoResp

        @Serializable
        data object LoginExpired : LoginInfoResp

        @Serializable
        data class Logined(val username: String, val id: Int) : LoginInfoResp
    }

    suspend fun loginInfo(token: Token?): LoginInfoResp

    @Serializable
    sealed interface LogoutResp {
        @Serializable
        data object Logout : LogoutResp
    }

    suspend fun logout(token: Token?): LogoutResp

    @Serializable
    sealed interface MineResp {
        @Serializable
        data class NormalUser(
            val username: String,
            val createAt: String,
            val acceptedTotal: Int,
            val accepted7Days: Int,
            val accepted30Days: Int,
            val acceptedToday: Int
        ) : MineResp

        @Serializable
        data class Administrator(val placeholder: Unit) : MineResp
    }

    suspend fun mine(token: Token?): MineResp
}