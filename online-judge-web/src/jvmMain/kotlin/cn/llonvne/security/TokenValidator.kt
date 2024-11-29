package cn.llonvne.security

import cn.llonvne.entity.AuthenticationUser

interface TokenValidator {
    suspend fun validate(
        token: Token?,
        action: suspend UserLoginLogoutTokenValidator.UserValidatorDsl.() -> Unit,
    ): AuthenticationUser?
}
