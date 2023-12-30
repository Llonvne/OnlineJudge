import arrow.continuations.SuspendApp
import arrow.continuations.ktor.server
import arrow.core.getOrElse
import arrow.fx.coroutines.resourceScope
import cn.llonvne.gojudge.app.judging
import cn.llonvne.gojudge.docker.GoJudgeResolver
import cn.llonvne.gojudge.docker.toJudgeContext
import cn.llonvne.gojudge.env.loadConfigFromEnv
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.server.netty.*
import kotlinx.coroutines.awaitCancellation
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private val json = Json {
    prettyPrint = true
}

fun main() = SuspendApp {
    val log = KotlinLogging.logger(name = "go-judger-main")
    log.info { "Initialization Go Judger ...." }

    val env = loadConfigFromEnv()

    resourceScope {
        val judgeContext = env.judgeSpec.map {
            log.info { json.encodeToString(it) }
            GoJudgeResolver(it).resolve().bind()
        }.getOrElse {
            throw RuntimeException("Failed to init judge")
        }.toJudgeContext()


        server(Netty, port = 8081) {
            judging(judgeContext)
        }


        awaitCancellation()
    }
}
