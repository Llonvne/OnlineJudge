import arrow.continuations.SuspendApp
import arrow.continuations.ktor.server
import arrow.core.raise.either
import arrow.fx.coroutines.resourceScope
import at.kopyk.Copy
import at.kopyk.CopyExtensions
import cn.llonvne.gojudge.api.GoJudgeEnvSpec
import cn.llonvne.gojudge.api.copy
import cn.llonvne.gojudge.app.judging
import cn.llonvne.gojudge.docker.configureGoJudgeContainer
import cn.llonvne.gojudge.docker.generateSecureKey
import com.fasterxml.jackson.annotation.JsonIgnore
import io.github.cdimascio.dotenv.Dotenv
import io.ktor.server.netty.*
import kotlinx.coroutines.awaitCancellation
import kotlinx.html.A
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encodeToString
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.Json
import org.slf4j.LoggerFactory


const val GO_JUDGE_PORT = 5050
const val GO_JUDGE_IP = "localhost"
private const val QUEUE = "demo-queue"

private const val ENABLE_JUDGE_TOKEN_AUTH = "JUDGE_AUTH_KEY_ENABLE"
private const val JUDGE_TOKEN = "JUDGE_AUTH_KEY"
private const val SECURE_TOKEN_LENGTH = 32

val logger = LoggerFactory.getLogger("JudgeManager")

fun Dotenv.isEnableJudgeAuthToken(): Boolean {
    return this.get(ENABLE_JUDGE_TOKEN_AUTH, "false").toBooleanStrictOrNull() ?: false
}



@JvmInline
value class Token(val token: String)

fun Dotenv.getToken() = either<String, Token> {
    val token = this@getToken.get(JUDGE_TOKEN, generateSecureKey(SECURE_TOKEN_LENGTH * 2))
    if (token.length < SECURE_TOKEN_LENGTH) {
        raise("token is too weak")
    }
    Token(token)
}


fun main() = SuspendApp {
//    val env = Dotenv.load()

    resourceScope {
        val container = install(
            acquire = {
                val container = configureGoJudgeContainer {
//                    this.fileTimeout = GoJudgeEnvSpec.FileTimeoutSetting.Timeout(30)
                }
                container.start()
                container
            }) { container, _ ->
            container.close()
        }


        server(Netty) {
            judging { }
        }
        awaitCancellation()
    }
}
