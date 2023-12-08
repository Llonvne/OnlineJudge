package cn.llonvne.gojudge.api

import de.jensklingenberg.ktorfit.Ktorfit
import de.jensklingenberg.ktorfit.converter.Converter
import de.jensklingenberg.ktorfit.http.*
import io.ktor.client.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.reflect.KClass
import kotlin.reflect.full.isSubclassOf

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

object GoJudgeFileConverterFactory : Converter.Factory {
    override fun requestParameterConverter(
        parameterType: KClass<*>,
        requestType: KClass<*>
    ): Converter.RequestParameterConverter? {

        if (parameterType.isSubclassOf(GoJudgeFile::class)) {
            return null
        }

        return object : Converter.RequestParameterConverter {
            override fun convert(data: Any): Any {
                return Json.encodeToString(data)
            }
        }
    }
}

val goJudgeClient by lazy {
    Ktorfit.Builder()
        .httpClient(ktorClient)
        .baseUrl("http://$GO_JUDGE_IP:$GO_JUDGE_PORT/")
        .converterFactories(
            GoJudgeFileConverterFactory
        )
        .build().create<GoJudgeService>()
}

interface GoJudgeService {
    @GET(GoJudgeRestEndPoint.VERSION)
    suspend fun version(@ReqBuilder ext: HttpRequestBuilder.() -> Unit = {}): String

    @GET(GoJudgeRestEndPoint.CONFIG)
    suspend fun config(@ReqBuilder ext: HttpRequestBuilder.() -> Unit = {}): String

    @POST(GoJudgeRestEndPoint.RUN)
    @Headers("Content-Type: application/json")
    suspend fun run(@Body request: RequestType.Request): List<Result>
}

internal object GoJudgeRestEndPoint {
    const val VERSION = "version"

    const val CONFIG = "config"

    const val RUN = "run"
}