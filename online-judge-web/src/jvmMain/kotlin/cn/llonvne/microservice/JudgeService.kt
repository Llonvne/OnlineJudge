package cn.llonvne.microservice

import cn.llonvne.gojudge.api.LanguageDispatcher
import cn.llonvne.gojudge.api.LanguageFactory
import cn.llonvne.gojudge.api.SupportLanguages
import cn.llonvne.gojudge.api.task.Output
import org.springframework.stereotype.Service

@Service
class JudgeService(
    private val languageDispatcher: LanguageDispatcher =
        LanguageDispatcher.get(
            LanguageFactory.get("localhost:8081")
        )
) {
    suspend fun judge(languages: SupportLanguages, stdin: String, code: String): Output {
        return languageDispatcher.dispatch(languages) {
            judge(code, stdin)
        }
    }
}