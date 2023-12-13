package cn.llonvne.gojudge.app

import arrow.core.Option
import cn.llonvne.gojudge.api.task.gpp.installGpp
import cn.llonvne.gojudge.docker.ContainerWrapper
import cn.llonvne.gojudge.ktor.RACE_LIMIT_JUDGE_NAME
import cn.llonvne.gojudge.ktor.globalAuth
import cn.llonvne.gojudge.ktor.installKtorOfficialPlugins
import cn.llonvne.gojudge.web.installManageWeb
import io.ktor.client.*
import io.ktor.server.application.*
import io.ktor.server.plugins.ratelimit.*
import io.ktor.server.routing.*


class JudgerConfig(val container: Option<ContainerWrapper>)

fun Application.judging(configuration: JudgerConfig) {
    installKtorOfficialPlugins()
    installManageWeb()
    routing {
        globalAuth {
            rateLimit(RACE_LIMIT_JUDGE_NAME) {

                configuration.container.onSome {
                    installGpp(it)
                }
            }
        }
    }
}
