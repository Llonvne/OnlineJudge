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
        val message: Message

        @Serializable
        data object BannedUser : LoginResult {
            override val message: Message
                get() = ToastMessage(MessageLevel.Danger, "您的账号已经被管理员封禁")
        }

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

    suspend fun logout(token: AuthenticationToken?): GetLogoutResp

    @Serializable
    sealed interface MineResp {
        @Serializable
        data class NormalUserMineResp(
            val username: String,
            val createAt: String,
            val acceptedTotal: Int,
            val accepted7Days: Int,
            val accepted30Days: Int,
            val acceptedToday: Int
        ) : MineResp

        @Serializable
        data class AdministratorMineResp(val placeholder: Unit) : MineResp
    }

    suspend fun mine(value: AuthenticationToken?): MineResp
}