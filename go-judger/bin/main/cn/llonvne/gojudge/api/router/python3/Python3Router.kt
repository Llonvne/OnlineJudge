package cn.llonvne.gojudge.api.router.python3

import cn.llonvne.gojudge.api.router.LanguageRouter
import cn.llonvne.gojudge.api.task.CodeInput
import cn.llonvne.gojudge.api.task.python.python3CompileTask
import cn.llonvne.gojudge.docker.JudgeContext
import cn.llonvne.gojudge.judgeClient
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.util.pipeline.*

context(JudgeContext)
internal fun python3() = object : LanguageRouter {

    val pythonCompileTask = python3CompileTask()

    context(PipelineContext<Unit, ApplicationCall>) override suspend fun version() {
        call.respondText(exec("python3 --version").stderr, contentType = ContentType.Application.Json)
    }

    context(PipelineContext<Unit, ApplicationCall>) override suspend fun judge(code: String, stdin: String) {
        val result = pythonCompileTask.run(CodeInput(code, stdin), judgeClient)
        call.respond(result)
    }
}