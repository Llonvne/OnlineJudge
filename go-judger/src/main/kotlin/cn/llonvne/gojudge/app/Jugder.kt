package cn.llonvne.gojudge.app

import cn.llonvne.gojudge.api.router.gpp.gpp
import cn.llonvne.gojudge.api.router.installLanguageRouter
import cn.llonvne.gojudge.api.router.java.java
import cn.llonvne.gojudge.docker.JudgeContext
import cn.llonvne.gojudge.docker.invoke
import cn.llonvne.gojudge.internal.config
import cn.llonvne.gojudge.judgeClient
import cn.llonvne.gojudge.ktor.RACE_LIMIT_JUDGE_NAME
import cn.llonvne.gojudge.ktor.globalAuth
import cn.llonvne.gojudge.ktor.installKtorOfficialPlugins
import cn.llonvne.gojudge.web.links.get
import cn.llonvne.gojudge.web.links.linkTr
import cn.llonvne.gojudge.web.links.linkTrUri
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.ratelimit.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

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
                    installLanguageRouter("gpp", "/gpp", "", gpp())
                    installLanguageRouter("java", "/java", "", java())
                    get("config", "config") {
                        call.respondText(judgeClient.config(), ContentType.Application.Json)
                    }
                }

                globalAuth {
                    rateLimit(RACE_LIMIT_JUDGE_NAME) {

                    }
                }
            }
        }
    }
}
