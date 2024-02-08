package cn.llonvne.security

import cn.llonvne.entity.AuthenticationUser
import cn.llonvne.redis.Redis
import cn.llonvne.redis.get
import cn.llonvne.redis.set
import com.benasher44.uuid.uuid4
import kotlinx.serialization.Serializable
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class RedisBasedAuthenticationToken(
    private val redis: Redis,
    private val passwordEncoder: PasswordEncoder
) {

    private val AuthenticationUser.asRedisKey get() = "user-id-$id-" + UUID.randomUUID()

    @Serializable
    data class RedisToken(internal val token: String)
    suspend fun login(user: AuthenticationUser): RedisToken {

        val token = user.asRedisKey

        redis.set(passwordEncoder.encode(token), user)

        return RedisToken(token)
    }
    suspend fun isLogin(token: RedisToken): Boolean {
        val result: AuthenticationUser = redis.get(token.token) ?: return false
        return true
    }
}