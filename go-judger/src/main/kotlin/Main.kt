import cn.llonvne.gojudge.app.judging
import io.ktor.server.engine.*
import io.ktor.server.netty.*

const val GO_JUDGE_PORT = 5050
const val GO_JUDGE_IP = "localhost"
fun main() {
    embeddedServer(Netty, port = 3000) {
        judging { }
    }.start(wait = true)
}