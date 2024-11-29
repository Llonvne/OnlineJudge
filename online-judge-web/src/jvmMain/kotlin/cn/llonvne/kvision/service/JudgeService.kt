package cn.llonvne.kvision.service

import cn.llonvne.gojudge.api.JudgeServerApi
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
@Suppress("ACTUAL_WITHOUT_EXPECT", "SpringJavaInjectionPointsAutowiringInspection")
actual class JudgeService(
    env: Environment,
    private val judgeUrl: String = env.getProperty("oj.url") ?: throw RuntimeException("无法获得 oj.url "),
    private val languageDispatcher: LanguageDispatcher = default(judgeUrl),
    private val judgeServerApi: JudgeServerApi = JudgeServerApi.get(judgeUrl, httpClient),
) : IJudgeService {
    override suspend fun judge(
        languages: SupportLanguages,
        stdin: String,
        code: String,
    ): Output =
        languageDispatcher.dispatch(languages) {
            judge(code, stdin)
        }

    suspend fun info() = judgeServerApi.info()

    companion object {
        private val httpClient: HttpClient =
            HttpClient(OkHttp) {
                install(ContentNegotiation) {
                    json(Json)
                }
            }

        private fun default(judgeUrl: String): LanguageDispatcher =
            LanguageDispatcher.get(
                LanguageFactory.get(
                    judgeUrl,
                    httpClient = httpClient,
                ),
            )
    }
}
