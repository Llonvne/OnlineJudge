package cn.llonvne.gojudge.app

import cn.llonvne.gojudge.api.router.gpp.installGpp
import cn.llonvne.gojudge.ktor.RACE_LIMIT_JUDGE_NAME
import cn.llonvne.gojudge.ktor.globalAuth
import cn.llonvne.gojudge.ktor.installKtorOfficialPlugins
import cn.llonvne.gojudge.web.installManageWeb
import cn.llonvne.gojudge.web.links.linkTr
import io.ktor.server.application.*
import io.ktor.server.plugins.ratelimit.*
import io.ktor.server.routing.*


fun Application.judging() {
    installKtorOfficialPlugins()
    installManageWeb()

    routing {
        linkTr("/link") {
            installGpp()
            globalAuth {
                rateLimit(RACE_LIMIT_JUDGE_NAME) {}
            }
        }
    }
}
