package cn.llonvne.gojudge.internal

import cn.llonvne.gojudge.api.spec.runtime.RequestType
import cn.llonvne.gojudge.api.spec.runtime.Result
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*

suspend fun GoJudgeClient.version(): String = httpClient.get("/version").body()

suspend fun GoJudgeClient.config(): String = httpClient.get("/config").body()

suspend fun GoJudgeClient.run(request: RequestType.Request): List<Result> {
    val resp: List<Result> =
        httpClient
            .post("/run") {
                setBody(request)
                contentType(ContentType.Application.Json)
            }.body()
    return resp
}
