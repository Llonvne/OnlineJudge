package cn.llonvne.gojudge.api.router.gpp

import cn.llonvne.gojudge.api.router.LanguageRouter
import cn.llonvne.gojudge.api.task.CodeInput
import cn.llonvne.gojudge.api.task.gpp.gppCompileTask
import cn.llonvne.gojudge.docker.JudgeContext
import cn.llonvne.gojudge.judgeClient
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.util.pipeline.*

context(JudgeContext)
internal fun gpp() = object : LanguageRouter {

    val gppCompileTask = gppCompileTask()

    context(PipelineContext<Unit, ApplicationCall>)
    override suspend fun version() {
        val version = exec("g++ -v")
        call.respondText(version.stderr, contentType = ContentType.Application.Json)
    }

    context(PipelineContext<Unit, ApplicationCall>)
    override suspend fun judge(code: String, stdin: String) {
        val result = gppCompileTask.run(CodeInput(code, stdin), judgeClient)
        call.respond(result)
    }
}
