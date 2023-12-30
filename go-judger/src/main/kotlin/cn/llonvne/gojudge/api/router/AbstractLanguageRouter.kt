package cn.llonvne.gojudge.api.router

import io.ktor.server.routing.*
import cn.llonvne.gojudge.web.links.get
import cn.llonvne.gojudge.web.links.linkTr
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.util.pipeline.*

internal interface LanguageRouter {
    context(PipelineContext<Unit, ApplicationCall>)
    fun version()

    context(PipelineContext<Unit, ApplicationCall>)
    fun judge(code: String)

    context(PipelineContext<Unit, ApplicationCall>)
    fun playground()
}

context(Route)
private abstract class AbstractLanguageRouter(
    private val name: String, private val path: String,
    private val languageRouter: LanguageRouter, private val decr: String = name
) {
    init {
        route(path) {
            linkTr(
                url = "/link",
                name = name,
                decr = decr,
            ) {
                get("/version", "version") {
                    languageRouter.version()
                }

                post {
                    val code = call.receiveParameters()["code"]
                        ?: return@post call.respond(HttpStatusCode.BadRequest, "代码为空")
                    languageRouter.judge(code)
                }

                get("/playground", "Playground") {
                    languageRouter.playground()
                }
            }
        }
    }
}