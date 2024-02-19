package cn.llonvne.kvision.service

import cn.llonvne.gojudge.api.LanguageDispatcher
import cn.llonvne.gojudge.api.LanguageFactory
import cn.llonvne.gojudge.api.SupportLanguages
import cn.llonvne.gojudge.api.task.Output
import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import org.springframework.beans.factory.config.ConfigurableBeanFactory
import org.springframework.context.annotation.Scope
import org.springframework.core.env.Environment
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@Suppress("ACTUAL_WITHOUT_EXPECT")
actual class JudgeService(
    env: Environment,
    @Suppress("SpringJavaInjectionPointsAutowiringInspection")
    private val judgeUrl: String = env.getProperty("oj.url") ?: throw RuntimeException("无法获得 oj.url "),
    @Suppress("SpringJavaInjectionPointsAutowiringInspection")
    private val languageDispatcher: LanguageDispatcher = default(judgeUrl)
) : IJudgeService {

    override suspend fun judge(languages: SupportLanguages, stdin: String, code: String): Output {
        return languageDispatcher.dispatch(languages) {
            judge(code, stdin)
        }
    }

    companion object {
        private fun default(judgeUrl: String): LanguageDispatcher {
            return LanguageDispatcher.get(
                LanguageFactory.get(
                    judgeUrl,
                    httpClient = HttpClient(OkHttp) {
                        install(ContentNegotiation) {
                            json(Json)
                        }
                    })
            )
        }
    }
}