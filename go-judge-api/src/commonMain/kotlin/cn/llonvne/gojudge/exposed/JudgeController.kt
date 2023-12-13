package cn.llonvne.gojudge.exposed

import cn.llonvne.gojudge.api.spec.RequestType
import cn.llonvne.gojudge.api.spec.Result
import io.ktor.client.call.*
import io.ktor.client.request.*


suspend fun RuntimeService.version(): String = httpClient.get("/version").body()
suspend fun RuntimeService.config(): String = httpClient.get("/config").body()
suspend fun RuntimeService.run(request: RequestType.Request): List<Result> =
    httpClient.post("/run") {
        setBody(request)
        setContextTypeApplicationJson()
    }.body()

