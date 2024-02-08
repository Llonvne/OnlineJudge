package cn.llonvne.redis

import io.lettuce.core.RedisClient
import kotlinx.coroutines.future.await
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.springframework.stereotype.Component
import kotlin.experimental.ExperimentalTypeInference

interface Redis {
    suspend fun getString(key: String): String?

    suspend fun set(key: String, value: String): String
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
    private val redisClient = RedisClient.create("redis://localhost:6379").connect().async()

    override suspend fun getString(key: String): String? {
        return redisClient.get(key).await()
    }

    override suspend fun set(key: String, value: String): String {
        return redisClient.set(key, value).await()
    }
}