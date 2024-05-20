package cn.llonvne.security

import cn.llonvne.entity.AuthenticationUser
import cn.llonvne.entity.role.Banned
import cn.llonvne.entity.role.Role
import cn.llonvne.getLogger
import cn.llonvne.redis.Redis
import cn.llonvne.redis.get
import cn.llonvne.redis.set
import org.slf4j.Logger
import org.springframework.core.env.Environment
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import java.util.*

/**
 * 基于 Redis 的登入服务类
 * [redis] 提供 Redis 实现，默认 Redis 实现在 [Redis],使用 kotlinx-serialization 进行序列化
 * [passwordEncoder] 提供加密方案
 */
@Service
class UserLoginLogoutTokenValidator(
    private val redis: Redis,
    private val passwordEncoder: PasswordEncoder,
    env: Environment,
    @Suppress("SpringJavaInjectionPointsAutowiringInspection")
    private val redisKeyPrefix: String = env.getProperty("oj.redis.user.prefix") ?: "user-id"
) : TokenValidator, LoginLogoutResolver {

    val log: Logger = getLogger()
    private val AuthenticationUser.asRedisKey get() = "$redisKeyPrefix-$id-" + UUID.randomUUID()

    override suspend fun login(user: AuthenticationUser): Token {

        val token = user.asRedisKey

        redis.set(token, user)

        return RedisToken(user.id, token)
    }

    suspend fun isLogin(token: Token?): Boolean {
        val user = getAuthenticationUser(token) ?: return false
        return !user.check(Banned.BannedImpl)
    }

    suspend fun getAuthenticationUser(id: Int): AuthenticationUser? {
        return redis.keys("$redisKeyPrefix-$id-*").firstOrNull()?.let { redis.get(it) }
    }

    suspend fun getAuthenticationUser(token: Token?): AuthenticationUser? {
        if (token == null) {
            return null
        }

        return redis.get(token.token)
    }

    override suspend fun logout(token: Token?) {
        if (token != null) {
            redis.clear(token.token)
        }
    }

    fun interface Validator {
        suspend fun validate(): Boolean
    }

    inner class UserValidatorDsl(
        val token: Token?
    ) {

        private val validators = mutableListOf<Validator>()

        /**
         * 检查的 ID 将会在日日志中展示用于 Debug
         */
        val validateId = UUID.randomUUID().toString().substring(0..6)

        /**
         * 要求用户为登入状态
         */
        suspend fun requireLogin() {
            addValidator {
                isLogin(token)
            }
        }

        /**
         * 检查用户权限中是否包含 [required],该方案将自动检查用户是否登入，未登入时将自动失败
         */
        suspend inline fun <reified R : Role> check(required: R) {
            addValidator {

                val user = getAuthenticationUser(token) ?: return@addValidator false

                log.info("[$validateId] require $required for user ${user.id} provides ${user.userRole}")

                user.check(required).also {
                    if (!it) {
                        log.info("[$validateId] check failed")
                    }
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

    override suspend fun validate(
        token: Token?, action: suspend UserValidatorDsl.() -> Unit
    ): AuthenticationUser? {

        val userValidatorDsl = UserValidatorDsl(token)

        log.info("[${userValidatorDsl.validateId}] validate for $token")

        userValidatorDsl.action()
        return if (!userValidatorDsl.result()) {
            null
        } else {
            log.info("[${userValidatorDsl.validateId}] pass")
            getAuthenticationUser(token)
        }
    }

    suspend fun update(user: AuthenticationUser) {
        redis.keys("$redisKeyPrefix-${user.id}-*").singleOrNull()
            ?.let {
                redis.set(it, user)
            }
    }
}