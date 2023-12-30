package cn.llonvne.gojudge.api.router.java

import cn.llonvne.gojudge.api.router.LanguageRouter
import cn.llonvne.gojudge.api.router.playground
import cn.llonvne.gojudge.api.task.CodeInput
import cn.llonvne.gojudge.api.task.java.JavaCompileTask
import cn.llonvne.gojudge.internal.version
import cn.llonvne.gojudge.service.runtimeService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.html.*
import io.ktor.server.response.*
import io.ktor.util.pipeline.*
import kotlinx.html.body

internal fun java() = object : LanguageRouter {

    val javaCompileTask = JavaCompileTask()

    context(PipelineContext<Unit, ApplicationCall>)  override suspend fun version() {
        val version = runtimeService.version()
        call.respondText(version, contentType = ContentType.Application.Json)
    }

    context(PipelineContext<Unit, ApplicationCall>) override suspend fun judge(code: String, stdin: String) {
        val result = javaCompileTask.run(CodeInput(code, stdin), runtimeService)
        call.respond(result)
    }

    context(PipelineContext<Unit, ApplicationCall>) override suspend fun playground() {
        call.respondHtml {
            body {
                this.playground("java", "/java")
            }
        }
    }
}
