package cn.llonvne.gojudge.app

import cn.llonvne.gojudge.api.SupportLanguages
import cn.llonvne.gojudge.api.router.gpp.gpp
import cn.llonvne.gojudge.api.router.install
import cn.llonvne.gojudge.api.router.installLanguageRouter
import cn.llonvne.gojudge.api.router.java.java
import cn.llonvne.gojudge.api.router.kotlin.kotlin
import cn.llonvne.gojudge.api.router.python3.python3
import cn.llonvne.gojudge.api.task.gpp.CppVersion
import cn.llonvne.gojudge.docker.JudgeContext
import cn.llonvne.gojudge.docker.invoke
import cn.llonvne.gojudge.internal.config
import cn.llonvne.gojudge.judgeClient
import cn.llonvne.gojudge.ktor.installKtorOfficialPlugins
import cn.llonvne.gojudge.web.links.LinkTreeConfigurer
import cn.llonvne.gojudge.web.links.get
import cn.llonvne.gojudge.web.links.linkTr
import cn.llonvne.gojudge.web.links.linkTrUri
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

context(JudgeContext, LinkTreeConfigurer, Routing)
fun installAllGpp() {
    SupportLanguages.entries
        .filter {
            it.name.startsWith("Cpp")
        }
        .forEach { supportLanguage ->
            with(supportLanguage) {
                installLanguageRouter(
                    languageName + languageVersion, "/$path", decr = "",
                    gpp(CppVersion.valueOf("Cpp$languageVersion"))
                )
            }
        }
}

/**
 * @param judgeContext 评测机运行时
 */
fun Application.judging(judgeContext: JudgeContext) {
    installKtorOfficialPlugins()

    routing {
        linkTrUri("/link") {
            get { call.respondRedirect(linkTreeUri) }

            linkTr {
                judgeContext {
                    installAllGpp()
                    install(SupportLanguages.Java, java())
                    install(SupportLanguages.Python3, python3())
                    install(SupportLanguages.Kotlin, kotlin())
                    get("config", "config") {
                        call.respondText(judgeClient.config(), ContentType.Application.Json)
                    }
                }
            }
        }
    }
}
