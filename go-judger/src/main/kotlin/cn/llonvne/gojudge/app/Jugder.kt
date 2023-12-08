package cn.llonvne.gojudge.app

import cn.llonvne.gojudge.ktor.*
import cn.llonvne.gojudge.web.installManageWeb
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.plugins.autohead.*
import io.ktor.server.plugins.callloging.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.doublereceive.*
import io.ktor.server.plugins.ratelimit.*
import io.ktor.server.plugins.requestvalidation.*
import io.ktor.server.request.*
import io.ktor.server.resources.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.json.Json
import org.slf4j.event.Level

class JudgerConfig {

}


fun Application.judging(configuration: JudgerConfig.() -> Unit) {
    install(Resources)
    install(AutoHeadResponse)
    install(RequestValidation)
    install(ContentNegotiation) {
        json(Json {
            prettyPrint = true
        })
    }
    install(DoubleReceive)
    install(CallLogging) {
        level = Level.INFO
        filter { call -> call.request.path().startsWith("/") }
    }
    installJudgeStatusPage()
    installJudgeRateLimit()
    installAuthentication()
    installCompression()
    installMicrometer()
    installCORS()
    installManageWeb()

    val config = JudgerConfig()

    config.configuration()


    routing {
        globalAuth {
            rateLimit(RACE_LIMIT_JUDGE_NAME) {
                get("/version") {
                    call.respondText("Hello")
                }
            }
        }
    }
}
