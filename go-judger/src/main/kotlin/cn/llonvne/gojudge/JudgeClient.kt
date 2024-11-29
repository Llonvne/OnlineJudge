package cn.llonvne.gojudge

import cn.llonvne.gojudge.internal.GoJudgeClient
import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.plugins.resources.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

internal val judgeClient by lazy {
    GoJudgeClient(
        HttpClient(OkHttp) {
            engine {
            }
            install(Logging) {
            }
            install(Resources) {}
            install(ContentNegotiation) {
                json(
                    Json {
                        isLenient = true
                        prettyPrint = true
                    },
                )
            }
            defaultRequest {
                port = 5050
                host = "localhost"
            }
        },
    )
}
