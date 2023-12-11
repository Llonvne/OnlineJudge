package cn.llonvne.gojudge.service

import cn.llonvne.gojudge.api.GoJudgeService
import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.resources.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json


val ktorClient = HttpClient(OkHttp) {
    engine {
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

val goJudgeService by lazy {
    GoJudgeService(ktorClient)
}