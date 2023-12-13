package cn.llonvne.gojudge.api.task

import cn.llonvne.gojudge.api.spec.Cmd
import cn.llonvne.gojudge.api.spec.RequestType
import cn.llonvne.gojudge.api.spec.Result
import cn.llonvne.gojudge.api.spec.Status
import cn.llonvne.gojudge.exposed.RuntimeService
import cn.llonvne.gojudge.exposed.run
import cn.llonvne.gojudge.services.runtime.request
import com.benasher44.uuid.uuid4
import kotlinx.serialization.Serializable

fun <T : Any, R> T?.map(transform: (T) -> R?): R? = this?.let(transform)

abstract class AbstractTask<I : Input> {
    abstract val sourceCodeExtension: String
    abstract val compiledFileExtension: String
    abstract fun getCompileCmd(input: I, filenames: Filenames): Cmd
    abstract fun getRunCmd(compileResult: Result, runFilename: Filename, runFileId: String): Cmd

    @Serializable
    sealed interface HookError<E, R> {
        @Serializable
        data class Error<E, R>(val output: E) : HookError<E, R>

        @Serializable
        data class Resume<E, R>(val result: R) : HookError<E, R>
    }

    @Serializable
    data class Filenames(val sourceCodeFilename: Filename, val compileFilename: Filename)

    @Serializable
    data class Filename(val name: String, val extension: String) {
        fun asString() = "$name.$extension"
    }

    /**
     * 在生成完文件名，可以调用该函数进行修改
     * 生成文件名依赖的下列拓展名已被包含在文件中
     * [sourceCodeExtension]
     * [compiledFileExtension]
     */
    open fun hookOnFilenames(filenames: Filenames): Filenames {
        return filenames
    }

    /**
     * 在构建完 runRequest 使用 [getCompileCmd] 后
     * 调用该函数对 request 进行修改
     */
    open fun hookOnBeforeCompile(request: RequestType.Request) {}

    // 该钩子函数只会处理存在结果的时候，如果不存在该函数不会被调用
    open fun hookOnCompileResult(result: Result) {}
    open fun transformCompileResultError(
        request: RequestType.Request, expectOutput: Output
    ): HookError<Output, Result> {
        return HookError.Error(expectOutput)
    }

    open fun transformCompileStatus(compileStatus: Status, compileResult: Result): Status {
        return compileStatus
    }

    open fun expectCompileStatus(): Status {
        return Status.Accepted
    }

    open fun transformRunFilename(filename: Filename) = filename

    open fun transformRunRequest(request: RequestType.Request): RequestType.Request {
        return request
    }

    open fun transformRunResult(request: RequestType.Request, result: Result) = result

    open fun transformRunError(request: RequestType.Request, expect: Output): Output {
        return expect
    }

    open fun transformRunSuccess(result: Result, expectOutput: Output): Output {
        return expectOutput
    }

    open fun hookOnReturnRunOutput(expectOutput: Output) {

    }

    suspend fun run(input: I, service: RuntimeService): Output {

        val filenames = hookOnFilenames(
            Filenames(
                Filename(uuid4().toString(), sourceCodeExtension),
                Filename(uuid4().toString(), compiledFileExtension)
            )
        )

        val compileRequest = request {
            add(getCompileCmd(input, filenames))
        }

        hookOnBeforeCompile(compileRequest)

        val compileResult = service.run(compileRequest).getOrNull(0)
            ?: when (val result =
                transformCompileResultError(compileRequest, Output.Failure.CompileResultIsNull(compileRequest))) {
                is HookError.Error -> return result.output
                is HookError.Resume -> result.result
            }

        hookOnCompileResult(compileResult)

        if (transformCompileStatus(compileResult.status, compileResult) != expectCompileStatus()) {
            return Output.Failure.CompileError(compileRequest, compileResult)
        }


        val fileId =
            compileResult.fileIds?.get(filenames.compileFilename.asString())
                ?: return Output.Failure.TargetFileNotExist(
                    compileRequest,
                    compileResult
                )


        val runFilename = transformRunFilename(Filename(uuid4().toString(), ""))

        val runRequest = transformRunRequest(request {
            getRunCmd(compileResult, runFilename, fileId)
        })

        val a: String? = null
        val b = a?.map { it.length }

        return when (val result = service.run(runRequest).getOrNull(0)) {
            null -> transformRunError(
                compileRequest, Output.Failure.RunResultIsNull(
                    compileRequest,
                    compileResult,
                    runRequest
                )
            )

            else -> transformRunSuccess(
                result,
                Output.Success(
                    compileRequest,
                    compileResult,
                    runRequest,
                    transformRunResult(runRequest, result)
                )
            )
        }.also {
            hookOnReturnRunOutput(it)
        }
    }
}