package cn.llonvne.gojudge.api.task.gpp

import cn.llonvne.gojudge.internal.version
import cn.llonvne.gojudge.service.runtimeService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.html.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.html.*

fun Route.installGpp() {
    val gppCompileTask = GppCompileTask()

    post("/gpp") {
        println(call.receiveText())
    }

    get("/version") {
        val version = runtimeService.version()
        call.respondHtml {
            body {
                h1 {
                    +"GppRouter plugin installed successfully!"
                }

                p {
                    +version
                }

                form {
                    method = FormMethod.post
                    action = "/gpp"

                    textArea {

                    }
                }
            }
        }
    }
    post("/run") {
        kotlin.runCatching { call.receive<GppInput>() }
            .onFailure {
                call.respond(HttpStatusCode.BadRequest, it)
            }
            .onSuccess {
                gppCompileTask.run(it, runtimeService)
            }
    }
}
