package cn.llonvne.gojudge.docker

import cn.llonvne.gojudge.api.GoJudgeEnvSpec
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.testcontainers.DockerClientFactory
import java.io.File
import kotlin.Result.Companion.failure
import kotlin.Result.Companion.success


class GoJudgeResolver(private val spec: GoJudgeEnvSpec) {
    private val charset = Charsets.UTF_8

    @Serializable
    data class CachedJsonClient(val spec: GoJudgeEnvSpec, val id: String) {
        private val log = KotlinLogging.logger { }
        private val dockerClient = DockerClientFactory.lazyClient()

        suspend fun start(): Result<GoJudgeEnvSpec> {
            return withContext(dockerCoroutinesContext) {
                try {
                    dockerClient.startContainerCmd(id).exec()
                    success(spec)
                } catch (e: Exception) {
                    log.error { "failed to start container from client json" }
                    failure(e)
                }
            }
        }
    }

    suspend fun resolve(): GoJudgeEnvSpec {
        val file = File("client.json")
        if (file.exists() && file.isFile && file.extension == "json" && file.canRead()) {
            val content = file.readText(charset)
            val client: CachedJsonClient = Json.decodeFromString(content)
            val result = client.start()
            return if (result.isFailure) {
                configureGoJudgeContainer(spec = spec)
                spec
            } else {
                client.spec
            }
        }

        configureGoJudgeContainer(spec = spec)
        return spec
    }
}