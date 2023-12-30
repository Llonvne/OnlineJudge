package cn.llonvne.gojudge.api.router.gpp

import cn.llonvne.gojudge.api.router.LanguageRouter
import cn.llonvne.gojudge.api.router.playground
import cn.llonvne.gojudge.api.task.CodeInput
import cn.llonvne.gojudge.api.task.gpp.GppCompileTask
import cn.llonvne.gojudge.internal.version
import cn.llonvne.gojudge.service.runtimeService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.html.*
import io.ktor.server.response.*
import io.ktor.util.pipeline.*
import kotlinx.html.*

internal fun gpp() = object : LanguageRouter {

    val gppCompileTask = GppCompileTask()

    context(PipelineContext<Unit, ApplicationCall>)
    override suspend fun version() {
        val version = runtimeService.version()
        call.respondText(version, contentType = ContentType.Application.Json)
    }

    context(PipelineContext<Unit, ApplicationCall>)
    override suspend fun judge(code: String, stdin: String) {
        val result = gppCompileTask.run(CodeInput(code, stdin), runtimeService)
        call.respond(result)
    }

    context(PipelineContext<Unit, ApplicationCall>)
    override suspend fun playground() {
        call.respondHtml {
            body {
               this.playground("C++","/gpp")
            }
        }
    }
}
