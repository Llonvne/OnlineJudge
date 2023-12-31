package cn.llonvne.gojudge.api.router.java

import cn.llonvne.gojudge.api.router.LanguageRouter
import cn.llonvne.gojudge.api.task.CodeInput
import cn.llonvne.gojudge.api.task.java.javaCompileTask
import cn.llonvne.gojudge.docker.JudgeContext
import cn.llonvne.gojudge.judgeClient
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.util.pipeline.*

context(JudgeContext)
internal fun java() = object : LanguageRouter {

    val javaCompileTask = javaCompileTask()

    context(PipelineContext<Unit, ApplicationCall>)  override suspend fun version() {
        val version = exec("java -version").stderr
        call.respondText(version, contentType = ContentType.Application.Json)
    }

    context(PipelineContext<Unit, ApplicationCall>) override suspend fun judge(code: String, stdin: String) {
        val result = javaCompileTask.run(CodeInput(code, stdin), judgeClient)
        call.respond(result)
    }
}
