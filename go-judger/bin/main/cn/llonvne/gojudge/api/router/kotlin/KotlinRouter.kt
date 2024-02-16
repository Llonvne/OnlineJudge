package cn.llonvne.gojudge.api.router.kotlin

import cn.llonvne.gojudge.api.router.LanguageRouter
import cn.llonvne.gojudge.api.task.CodeInput
import cn.llonvne.gojudge.api.task.kotlin.kotlinCompileTask
import cn.llonvne.gojudge.docker.JudgeContext
import cn.llonvne.gojudge.judgeClient
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.util.pipeline.*

context(JudgeContext)
internal fun kotlin() = object : LanguageRouter {

    val kotlinCompileTask = kotlinCompileTask()

    context(PipelineContext<Unit, ApplicationCall>) override suspend fun version() {
        call.respondText(exec("kotlinc -version").stderr, ContentType.Application.Json)
    }

    context(PipelineContext<Unit, ApplicationCall>) override suspend fun judge(code: String, stdin: String) {
        call.respond(kotlinCompileTask.run(CodeInput(code, stdin), judgeClient))
    }
}