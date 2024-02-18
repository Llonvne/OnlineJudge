package cn.llonvne.security

import cn.llonvne.entity.AuthenticationUser
import cn.llonvne.entity.role.Role
import cn.llonvne.redis.Redis
import cn.llonvne.redis.get
import cn.llonvne.redis.set
import org.slf4j.Logger
import org.slf4j.LoggerFactory.getLogger
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import java.util.*

@Service
class RedisAuthenticationService(
    private val redis: Redis,
    private val passwordEncoder: PasswordEncoder,
) {

    val log: Logger = getLogger(this::class.java)

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
        val token: AuthenticationToken?
    ) {

        private val validators = mutableListOf<Validator>()

        val validateId = UUID.randomUUID().toString().substring(0..6)

        suspend fun requireLogin() {
            addValidator {
                isLogin(token)
            }
        }

        suspend inline fun <reified R : Role> UserValidatorDsl.check(required: R) {
            addValidator {


                val user = getAuthenticationUser(token) ?: return@addValidator false

                log.info("[$validateId] require $required for user ${user.id} provides ${user.userRole}")

                user.check(required).also {
                    log.info("[$validateId] check failed")
                }
            }
        }

        fun addValidator(validator: Validator) {
            validators.add(validator)
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

        log.info("[${userValidatorDsl.validateId}] validate for $authenticationToken")

        userValidatorDsl.action()
        return if (!userValidatorDsl.result()) {
            null
        } else {
            getAuthenticationUser(authenticationToken)
        }
    }
}