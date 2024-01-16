package cn.llonvne.kvision.service

import cn.llonvne.security.AuthenticationToken
import io.kvision.annotations.KVService
import kotlinx.serialization.Serializable

@KVService
interface IAuthenticationService {
    @Serializable
    sealed interface RegisterResult {
        @Serializable
        data class SuccessfulRegistration(val token: AuthenticationToken) : RegisterResult

        @Serializable
        data class Failed(val reason: String) : RegisterResult
    }

    suspend fun register(username: String, password: String): RegisterResult
    suspend fun login(username: String, password: String): Boolean
}