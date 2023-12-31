package cn.llonvne.gojudge.api.router

import cn.llonvne.gojudge.web.links.LinkTreeConfigurer
import cn.llonvne.gojudge.web.links.get
import cn.llonvne.gojudge.web.links.linkIn
import cn.llonvne.gojudge.web.links.linkTr
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.util.pipeline.*

context(Routing)
internal fun LinkTreeConfigurer.installLanguageRouter(
    name: String,
    path: String,
    decr: String = name,
    languageRouter: LanguageRouter
) {
    linkIn(name, decr, "$path/link")
    LanguageRouterLoader(name, path, languageRouter, decr = decr)
}

/**
 * 定义了语言服务的标准接口
 */
internal interface LanguageRouter {
    /**
     * [version] 返回语言编译器/执行器的版本信息，默认使用 Application/Json
     */
    context(PipelineContext<Unit, ApplicationCall>)
    suspend fun version()

    /**
     * [judge] 传入代码，开始评测语言
     */
    context(PipelineContext<Unit, ApplicationCall>)
    suspend fun judge(code: String, stdin: String)

    context(PipelineContext<Unit, ApplicationCall>)
    suspend fun playground(languageName: String, judgePath: String)
}

context(Route)
private class LanguageRouterLoader(
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
                    val stdin = call.receiveParameters()["stdin"]
                        ?: return@post call.respond(HttpStatusCode.BadRequest, "输入为空")
                    languageRouter.judge(code, stdin)
                }

                get("/playground", "Playground") {
                    languageRouter.playground(name, path)
                }
            }
        }
    }
}