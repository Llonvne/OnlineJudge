package cn.llonvne.model

import cn.llonvne.kvision.service.IAuthenticationService
import io.kvision.remote.getService

object AuthenticationModel {
    private val authenticationService = getService<IAuthenticationService>()

    suspend fun register(username: String, password: String) = authenticationService.register(
        username, password
    )

    suspend fun login(username: String, password: String): Boolean = authenticationService.login(username, password)
}