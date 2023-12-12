package cn.llonvne.gojudge.api.task

import cn.llonvne.gojudge.api.spec.*
import cn.llonvne.gojudge.exposed.RuntimeService
import cn.llonvne.gojudge.exposed.run
import cn.llonvne.gojudge.services.runtime.request
import cn.llonvne.gojudge.services.runtime.useUsrBinEnv
import com.benasher44.uuid.uuid4
import kotlinx.serialization.Serializable

abstract class AbstractTask<I : Input> {
    abstract val sourceCodeExtension: String
    abstract val compiledFileExtension: String
    abstract fun getCompileCmd(input: I, filenames: Filenames): Cmd

    @Serializable
    data class Filenames(val sourceCodeFilename: Filename, val compileFilename: Filename)

    @Serializable
    data class Filename(val name: String, val extension: String)

    open fun hookOnFilenames(filenames: Filenames): Filenames {
        return filenames
    }

    open fun hookOnBeforeCompile(request: RequestType.Request) {}

    // 该钩子函数只会处理存在结果的时候，如果不存在该函数不会被调用
    open fun hookOnCompileResult(result: Result) {}

    open fun transformCompileResultError(
        request: RequestType.Request,
        expectOutput: Output
    ): HookError<Output, Result> {
        return HookError.Error(expectOutput)
    }

    open fun transfromCompileResultNotAccept(
        result: Result,
        status: Status
    ): HookError<Status, Output> {
        return AbstractTask.HookError.Error(status)
    }

    open fun transformCompileStatus(compileStatus: Status, compileResult: Result): Status {
        return compileStatus
    }

    @Serializable
    sealed interface HookError<E, R> {
        @Serializable
        data class Error<E, R>(val output: E) : HookError<E, R>

        @Serializable
        data class Resume<E, R>(val result: R) : HookError<E, R>
    }

    suspend fun run(input: I, service: RuntimeService): Output {

        val filenames = hookOnFilenames(
            Filenames(
                Filename(uuid4().toString(), sourceCodeExtension),
                Filename(uuid4().toString(), compiledFileExtension)
            )
        )

        val compileRequest = request {
            getCompileCmd(input, filenames)
        }

        hookOnBeforeCompile(compileRequest)

        val compileResult = service.run(compileRequest).getOrNull(0)
            ?: when (val result =
                transformCompileResultError(compileRequest, Output.Failure.CompileResultIsNull(compileRequest))) {
                is HookError.Error -> return result.output
                is HookError.Resume -> result.result
            }

        hookOnCompileResult(compileResult)

        if (transformCompileStatus(compileResult.status, compileResult) != Status.Accepted) {
            return Output.Failure.CompileError(compileRequest, compileResult)
        }

        val fileId =
            compileResult.fileIds?.get(compiledFilename) ?: return Output.Failure.TargetFileNotExist(
                compileRequest,
                compileResult
            )

        val runFilename = uuid4().toString()

        val runRequest = request {
            cmd {
                args = listOf(runFilename)
                env = useUsrBinEnv
                files = useStdOutErrForFiles(input.stdin)
                copyIn = useFileIdCopyIn(fileId = fileId, newName = runFilename)
            }
        }

        val runResult = service.run(runRequest).getOrNull(0) ?: return GppOutput.Failure.RunResultIsNull(
            compileRequest,
            compileResult,
            runRequest
        )

        return Output.Success(
            compileRequest,
            compileResult,
            runRequest,
            runResult
        )
    }
}