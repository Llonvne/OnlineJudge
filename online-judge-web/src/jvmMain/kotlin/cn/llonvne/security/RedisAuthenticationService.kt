package cn.llonvne.security

import cn.llonvne.entity.AuthenticationUser
import cn.llonvne.entity.role.Role
import cn.llonvne.redis.Redis
import cn.llonvne.redis.get
import cn.llonvne.redis.set
import cn.llonvne.security.RedisAuthenticationService.Validator
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import java.util.*

@Service
class RedisAuthenticationService(
    private val redis: Redis,
    private val passwordEncoder: PasswordEncoder,
) {

    private val AuthenticationUser.asRedisKey get() = "user-id-$id-" + UUID.randomUUID()

    suspend fun login(user: AuthenticationUser): AuthenticationToken {

        val token = user.asRedisKey

        redis.set(token, user)

        return RedisToken(user.id, token)
    }

    suspend fun isLogin(token: AuthenticationToken?): Boolean {
        return getAuthenticationUser(token) != null
    }

    suspend fun getAuthenticationUser(token: AuthenticationToken?): AuthenticationUser? {

        if (token == null) {
            return null
        }

        return redis.get(token.token)
    }

    suspend fun logout(token: AuthenticationToken?) {
        if (token != null) {
            redis.clear(token.token)
        }
    }

    fun interface Validator {
        suspend fun validate(): Boolean
    }

    inner class UserValidatorDsl(
        private val token: AuthenticationToken?
    ) {

        private val validators = mutableListOf<Validator>()

        suspend fun requireLogin() {
            addValidator {
                isLogin(token)
            }
        }

        suspend fun <R> requireUser(action: suspend (AuthenticationUser) -> R): R? {
            var ret: R? = null
            validators.add(Validator {
                val isLogin = isLogin(token)
                if (isLogin) {
                    getAuthenticationUser(token)?.let { ret = action(it) }
                }
                isLogin
            })
            return ret
        }

        private fun addValidator(validator: Validator) {
            validators.add(validator)
        }

        suspend fun requireRole(vararg required: Role) {
            requireUser {
                check {
                    provide(userRole = it.userRole)
                    require(required.toList())
                }
            }
        }


        internal suspend fun result(): Boolean {
            return validators.all { it.validate() }
        }
    }

    suspend fun validate(
        authenticationToken: AuthenticationToken?,
        action: suspend UserValidatorDsl.() -> Unit
    ): AuthenticationUser? {
        val userValidatorDsl = UserValidatorDsl(authenticationToken)
        userValidatorDsl.action()
        return if (!userValidatorDsl.result()) {
            null
        } else {
            getAuthenticationUser(authenticationToken)
        }
    }
}