package cn.llonvne.gojudge.env

import arrow.core.Either
import arrow.core.None
import arrow.core.Option
import arrow.core.Some
import arrow.core.raise.either
import cn.llonvne.gojudge.api.GoJudgeEnvSpec
import cn.llonvne.gojudge.api.isValidPort
import cn.llonvne.gojudge.docker.generateSecureKey
import com.github.dockerjava.zerodep.shaded.org.apache.hc.core5.net.InetAddressUtils
import io.github.cdimascio.dotenv.Dotenv
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private const val ENABLE_JUDGE_KEY = "JUDGE"
private const val ENABLE_JUDGE_TOKEN_AUTH = "JUDGE_AUTH_KEY_ENABLE"
private const val JUDGE_TOKEN = "JUDGE_AUTH_KEY"
private const val SECURE_TOKEN_LENGTH = 32
private const val GO_JUDGE_IP = "JUDGE_IP"
private const val GO_JUDGE_PORT = "JUDGE_PORT"

@RequiresOptIn(message = "It relay on KopyKat Generated Code!")
private annotation class RelayOnGeneratedCode

@RelayOnGeneratedCode
private fun GoJudgeEnvSpecRef.set(block: GoJudgeEnvSpec.() -> Unit) {

    TODO();
//    inner = inner.copy {
//        block()
//    }
}

@Serializable
internal data class GoJudgeEnvSpecRef(var inner: GoJudgeEnvSpec = GoJudgeEnvSpec()) {
    fun set(spec: GoJudgeEnvSpec) {
        this.inner = spec
    }
}

internal data class EnvConfig(val rawEnv: Dotenv, val judgeSpec: Option<GoJudgeEnvSpec>)

private val log = KotlinLogging.logger {}

internal fun loadConfigFromEnv(): EnvConfig {

    log.info { "loading envs from system ..." }

    val env = Dotenv.load()

    val spec = env.loadJudgeConfig()

    return EnvConfig(env, spec)
}


private val json = Json { prettyPrint = true }

@OptIn(RelayOnGeneratedCode::class)
private fun Dotenv.loadJudgeConfig(): Option<GoJudgeEnvSpec> {

    val spec = GoJudgeEnvSpecRef()

    ifNull(this.get(ENABLE_JUDGE_KEY, "false").toBooleanStrictOrNull()) {
        log.error { "go-judge is disabled,please confirm your setting!" }
        return None
    }

    setToken(spec)

    ifNotNull(this.get(GO_JUDGE_IP, "127.0.0.1")) { ipStr ->
        require(isValidIP(ipStr)) { "$ipStr is not a valid ip" }
        spec.set {
            TODO();
//            this.httpAddr.url = ipStr
        }
    }

    ifNotNull(this.get(GO_JUDGE_PORT, "5050")) { portStr ->
        val port = portStr.toIntOrNull()
        require(port != null && isValidPort(port)) {
            "$portStr is not valid port"
        }
        spec.set {
            TODO();
//            this.httpAddr.port = port
        }
    }

    println(message = Json { prettyPrint = true }.encodeToString(spec.inner))

    return Some(spec.inner)
}

internal class TokenIsTooWeak(value: String) : Exception(value)

fun Dotenv.isEnableJudgeAuthToken(): Boolean {
    return this.get(ENABLE_JUDGE_TOKEN_AUTH, "false").toBooleanStrictOrNull() ?: false
}

@JvmInline
value class Token(val token: String)

fun Dotenv.getToken() = either {
    val token = this@getToken.get(JUDGE_TOKEN, generateSecureKey(SECURE_TOKEN_LENGTH * 2))
    if (token.length < SECURE_TOKEN_LENGTH) {
        log.error { "token is too weak" }
        raise("token is too weak")
    }
    Token(token)
}

@OptIn(RelayOnGeneratedCode::class)
internal fun Dotenv.setToken(spec: GoJudgeEnvSpecRef) {
    val enableToken = isEnableJudgeAuthToken()
    if (enableToken) {
        val token = when (val token = getToken()) {
            is Either.Left -> throw TokenIsTooWeak(token.value)
            is Either.Right -> token.value
        }

        log.info { "judge token is ${token.token}" }

        spec.set {
            TODO()
//            this.authToken = GoJudgeEnvSpec.GoJudgeAuthTokenSetting.Enable(token.token)
        }
    }
}

private fun isValidIP(ip: String): Boolean {
    return InetAddressUtils.isIPv4Address(ip) || InetAddressUtils.isIPv4Address(ip)
}

inline fun <reified T> ifNotNull(value: T?, then: (T) -> Unit) {
    if (value != null) {
        then(value)
    }
}

inline fun <reified T> ifNull(value: T?, then: () -> Unit) {
    if (value == null) {
        then()
    }
}