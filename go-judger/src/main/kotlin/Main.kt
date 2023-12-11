import arrow.continuations.SuspendApp
import arrow.fx.coroutines.resourceScope
import cn.llonvne.gojudge.app.judging
import cn.llonvne.gojudge.docker.GoJudgeResolver
import cn.llonvne.gojudge.env.loadConfigFromEnv
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.server.netty.*
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.launch

fun main() = SuspendApp {
    val log = KotlinLogging.logger(name = "go-judger-main")

    log.info { "Initialization Go Judger ...." }

    val env = loadConfigFromEnv()

    resourceScope {

        launch {
            io.ktor.server.engine.embeddedServer(Netty) {
                judging { }
            }
        }

        launch {
            env.judgeSpec.map { GoJudgeResolver(it).resolve().bind() }
        }

        awaitCancellation()
    }
}