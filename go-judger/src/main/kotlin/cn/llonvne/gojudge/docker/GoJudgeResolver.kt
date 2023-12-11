package cn.llonvne.gojudge.docker

import arrow.fx.coroutines.Resource
import arrow.fx.coroutines.resource
import cn.llonvne.gojudge.api.gojudgespec.GoJudgeEnvSpec
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.json.Json
import org.testcontainers.DockerClientFactory
import java.io.File
import kotlin.Result.Companion.failure
import kotlin.Result.Companion.success


class GoJudgeResolver(private val spec: GoJudgeEnvSpec) {
    private val charset = Charsets.UTF_8
    private val log = KotlinLogging.logger { }

    @Serializable
    data class CachedJsonClient(val spec: GoJudgeEnvSpec, val id: String) {
        @Transient
        private val log = KotlinLogging.logger { }

        @Transient
        private val dockerClient = DockerClientFactory.lazyClient()

        suspend fun start(): Result<GoJudgeEnvSpec> {
            return withContext(dockerCoroutinesContext) {
                try {
                    val restartContainerCmd = dockerClient.restartContainerCmd(id)
                    restartContainerCmd.exec()
                    success(spec)
                } catch (e: Exception) {
                    log.error { "failed to start container from client json" }
                    failure(e)
                }
            }
        }

        suspend fun stop(): Unit = withContext(dockerCoroutinesContext) {
            dockerClient.stopContainerCmd(id).exec()
        }
    }

    suspend fun resolve(): Resource<*> {
        val file = File("client.json")
        if (file.exists() && file.isFile && file.extension == "json" && file.canRead()) {
            val content = file.readText(charset)

            val client: CachedJsonClient = try {
                Json.decodeFromString<CachedJsonClient>(content)
            } catch (e: Exception) {
                return configureGoJudgeContainer(spec = spec)
            }

            if (client.start().isFailure) {
                return configureGoJudgeContainer(spec = spec)
            } else {
                log.info { "use cached go-judge docker client" }
                return resource({}) { _, _ ->
                    client.stop()
                }
            }
        }
        return configureGoJudgeContainer(spec = spec)
    }
}