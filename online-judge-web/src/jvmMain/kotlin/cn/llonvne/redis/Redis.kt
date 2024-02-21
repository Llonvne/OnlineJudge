package cn.llonvne.redis

import io.lettuce.core.RedisClient
import io.lettuce.core.api.async.RedisAsyncCommands
import kotlinx.coroutines.future.await
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.springframework.stereotype.Component
import kotlin.experimental.ExperimentalTypeInference

interface Redis {
    suspend fun getString(key: String): String?

    suspend fun set(key: String, value: String): String

    suspend fun clear(key: String)

    suspend fun keys(pattern: String): Set<String>
}

@OptIn(ExperimentalTypeInference::class)
@OverloadResolutionByLambdaReturnType
internal suspend inline fun <reified T> Redis.get(key: String): T? {
    return this.getString(key).let { v ->
        runCatching {
            Json.decodeFromString<T>(v ?: return@let null)
        }.getOrNull()
    }
}

@OptIn(ExperimentalTypeInference::class)
@OverloadResolutionByLambdaReturnType
internal suspend inline fun <reified T> Redis.set(key: String, value: T): String {
    return set(key, Json.encodeToString(value))
}

@Component
private class RedisImpl : Redis {
    private val redisClient: RedisAsyncCommands<String, String> =
        RedisClient.create("redis://localhost:6379").connect().async()

    private suspend fun <R> onRedis(block: suspend RedisAsyncCommands<String, String>.() -> R): R {
        return block(redisClient)
    }

    override suspend fun getString(key: String) = onRedis {
        get(key).await()
    }

    override suspend fun set(key: String, value: String) = onRedis {
        set(key, value).await()
    }

    override suspend fun clear(key: String) = onRedis {
        del(key).await()
        Unit
    }

    override suspend fun keys(pattern: String): Set<String> = onRedis {
        keys(pattern).await().toSet()
    }
}