package cn.llonvne.gojudge.ktor

import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.plugins.autohead.*
import io.ktor.server.plugins.callloging.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.doublereceive.*
import io.ktor.server.plugins.requestvalidation.*
import io.ktor.server.request.*
import io.ktor.server.resources.*
import io.ktor.server.routing.*
import kotlinx.serialization.json.Json
import org.slf4j.event.Level

fun Application.installKtorOfficialPlugins() {
    install(Routing)
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
}