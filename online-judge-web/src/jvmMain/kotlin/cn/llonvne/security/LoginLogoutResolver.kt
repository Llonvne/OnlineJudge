package cn.llonvne.security

import cn.llonvne.entity.AuthenticationUser

interface LoginLogoutResolver {
    suspend fun logout(token: Token?)

    suspend fun login(user: AuthenticationUser): Token
}