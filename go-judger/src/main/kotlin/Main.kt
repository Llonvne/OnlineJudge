import arrow.continuations.SuspendApp
import arrow.continuations.ktor.server
import arrow.fx.coroutines.resourceScope
import cn.llonvne.gojudge.app.judging
import cn.llonvne.gojudge.docker.GoJudgeResolver
import cn.llonvne.gojudge.env.loadConfigFromEnv
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.server.netty.*
import kotlinx.coroutines.awaitCancellation


const val GO_JUDGE_PORT = 5050
const val GO_JUDGE_IP = "localhost"
private const val QUEUE = "demo-queue"


fun main() = SuspendApp {
    val log = KotlinLogging.logger(name = "go-judger-main")

    log.info { "Initialization Go Judger ...." }

    val env = loadConfigFromEnv()

    resourceScope {
        server(Netty) {
            judging { }
        }

        env.judgeSpec.map { GoJudgeResolver(it).resolve() }

        awaitCancellation()
    }
}
