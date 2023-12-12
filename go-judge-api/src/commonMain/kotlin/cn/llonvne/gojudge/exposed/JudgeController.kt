package cn.llonvne.gojudge.exposed

import cn.llonvne.gojudge.api.spec.RequestType
import cn.llonvne.gojudge.api.spec.Result
import de.jensklingenberg.ktorfit.http.GET
import de.jensklingenberg.ktorfit.http.POST
import io.ktor.client.call.*
import io.ktor.client.plugins.resources.*
import io.ktor.client.request.*
import io.ktor.resources.*

@Resource("/judge")
class JudgeController {
    @Resource("/version")
    class Version

    @Resource("/config")
    class Config

    @Resource("/run")
    class Run
}

interface Sample {
    @GET("/version")
    suspend fun version(): String

    @GET("/config")
    suspend fun config(): String

    @POST("/run")
    suspend fun run(request: RequestType.Request): List<RequestType.Request>
}

suspend fun RuntimeService.version(): String = httpClient.get(JudgeController.Version()).body()
suspend fun RuntimeService.config(): String = httpClient.get(JudgeController.Config()).body()
suspend fun RuntimeService.run(request: RequestType.Request): List<Result> =
    httpClient.post(JudgeController.Run()) {
        setBody(request)
        setContextTypeApplicationJson()
    }.body()

