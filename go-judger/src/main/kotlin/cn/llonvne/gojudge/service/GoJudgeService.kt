package cn.llonvne.gojudge.service

import cn.llonvne.gojudge.api.GoJudgeService
import io.ktor.client.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json



val ktorClient = HttpClient {
    install(ContentNegotiation) {
        json(Json {
            isLenient = true
            prettyPrint = true
        })
    }
    engine {
    }
}

val goJudgeService by lazy {
    GoJudgeService(ktorClient)
}
