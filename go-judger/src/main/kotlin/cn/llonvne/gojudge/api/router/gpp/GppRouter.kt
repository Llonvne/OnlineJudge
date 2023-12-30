package cn.llonvne.gojudge.api.router.gpp

import cn.llonvne.gojudge.api.task.CodeInput
import cn.llonvne.gojudge.api.task.gpp.GppCompileTask
import cn.llonvne.gojudge.internal.version
import cn.llonvne.gojudge.service.runtimeService
import cn.llonvne.gojudge.web.links.LinkTreeConfigurer
import cn.llonvne.gojudge.web.links.get
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.html.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.html.*

context(LinkTreeConfigurer)
fun Route.installGpp() {
    val gppCompileTask = GppCompileTask()

    route("/gpp") {

        // 重定向到 /link
        get {
            call.respondRedirect("/link")
        }

        // 运行代码
        post {
            val code = call.receiveParameters()["code"]
                ?: return@post call.respond(HttpStatusCode.BadRequest, "代码为空")
            val result = gppCompileTask.run(CodeInput(code, "1"), runtimeService)
            call.respond(result)
        }

        // 获取 version 信息
        get("/version", "GppVersion") {
            val version = runtimeService.version()
            call.respondText(version, contentType = ContentType.Application.Json)
        }

        // 进入 playground
        get("/playground", "PlayGround") {
            call.respondHtml {
                body {

                    h1 {
                        +"C++ Playground"
                    }

                    form {
                        method = FormMethod.post
                        action = "/gpp"

                        label {
                            htmlFor = "code"
                            +"Your Code here"
                        }

                        br { }

                        textArea {
                            id = "code"
                            name = "code"
                            required = true

                            cols = "30"
                            rows = "10"
                        }

                        input {
                            type = InputType.submit
                            value = "Submit"
                        }
                    }
                }
            }
        }
    }
}
