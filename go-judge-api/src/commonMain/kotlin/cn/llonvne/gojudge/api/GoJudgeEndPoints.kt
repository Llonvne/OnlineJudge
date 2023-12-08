package cn.llonvne.gojudge.api

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.resources.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.resources.*
import kotlin.jvm.JvmInline

@JvmInline
value class GoJudgeService(val httpClient: HttpClient)

@Resource("/version")
class Version

suspend fun GoJudgeService.version(): String = httpClient.get(Version()).body()

@Resource("/config")
class Config

suspend fun GoJudgeService.config(): String = httpClient.get(Config()).body()

@Resource("/run")
class Run

suspend fun GoJudgeService.run(request: RequestType.Request): List<Result> =
    httpClient.post(Run()) {
        setBody(request)
        setContextTypeApplicationJson()
    }.body()

fun HttpRequestBuilder.setContextTypeApplicationJson() {
    contentType(ContentType.parse("application/json"))
}