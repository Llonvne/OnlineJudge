package cn.llonvne.gojudge.service

import cn.llonvne.gojudge.exposed.RuntimeService
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.plugins.resources.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

internal val ktorClient = HttpClient(OkHttp) {
    engine {
    }
    install(Logging) {
    }
    install(Resources) {}
    install(ContentNegotiation) {
        json(Json {
            isLenient = true
            prettyPrint = true
        })
    }
    defaultRequest {
        port = 5050
        host = "localhost"
    }
}

val runtimeService = RuntimeService(ktorClient)