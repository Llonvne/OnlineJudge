import arrow.continuations.SuspendApp
import arrow.continuations.ktor.server
import arrow.fx.coroutines.resourceScope
import cn.llonvne.gojudge.app.judging
import cn.llonvne.gojudge.docker.GoJudgeResolver
import cn.llonvne.gojudge.env.loadConfigFromEnv
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.server.netty.*
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.launch
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
        val container = env.judgeSpec.map {
            log.info { json.encodeToString(it) }
            GoJudgeResolver(it).resolve().bind()
        }

        server(Netty, port = 8081) {
            judging()
        }

        launch {
            container.map {
                it.resolveDependencies()
            }
        }

        awaitCancellation()
    }
}
