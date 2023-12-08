package cn.llonvne.gojudge.api

import de.jensklingenberg.ktorfit.Ktorfit
import de.jensklingenberg.ktorfit.http.Body
import de.jensklingenberg.ktorfit.http.GET
import de.jensklingenberg.ktorfit.http.Headers
import de.jensklingenberg.ktorfit.http.POST
import io.ktor.client.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

interface GoJudgeService {
    @GET(GoJudgeRestEndPoint.VERSION)
    suspend fun version(): String

    @GET(GoJudgeRestEndPoint.CONFIG)
    suspend fun config(): String

    @POST(GoJudgeRestEndPoint.RUN)
    @Headers("Content-Type: application/json")
    suspend fun run(@Body request: RequestType.Request): List<Result>
}

object GoJudgeRestEndPoint {
    const val VERSION = "version"

    const val CONFIG = "config"

    const val RUN = "run"
}

const val GO_JUDGE_PORT = 5050

const val GO_JUDGE_IP = "localhost"

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


val goJudgeClient by lazy {
    Ktorfit.Builder()
        .httpClient(ktorClient)
        .baseUrl("http://$GO_JUDGE_IP:$GO_JUDGE_PORT/")
        .build().create<GoJudgeService>()
}
