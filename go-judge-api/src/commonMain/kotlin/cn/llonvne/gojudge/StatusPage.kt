package cn.llonvne.gojudge

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.html.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import kotlinx.html.body
import kotlinx.html.h1
import kotlinx.html.h4
import kotlinx.html.p

fun Application.installJudgeStatusPage() {
    install(StatusPages) {
        exception<Throwable> { call, cause ->
            call.respondText(text = "500: $cause", status = HttpStatusCode.InternalServerError)
        }

        status(HttpStatusCode.NotFound) { call, stat ->
            call.respondHtml {
                body {
                    h1 {
                        +"Not found"
                    }
                }
            }
        }

        status(HttpStatusCode.Unauthorized) { call, stat ->
            call.respondHtml(stat) {
                body {
                    h1 {
                        +"Unauthorized Request"
                    }
                }
            }
        }

        status(HttpStatusCode.TooManyRequests) { call, status ->
            val retryAfter = call.response.headers["Retry-After"]
            val permission = call.userJudgePermission
            call.respondHtml(status) {
                body {
                    h1 {
                        +"Too Many Request"
                    }
                    h4 {
                        +"wait for $retryAfter seconds to refill your token"
                    }
                    p {
                        +"you are ${permission.name},you only have $TOTAL_TOKEN_IN_DURATION tokens in in $JUDGE_TOKEN_REFILL_DURATION,you will cost ${permission.costTokenPer} for a request"
                    }
                }
            }
        }
    }
}