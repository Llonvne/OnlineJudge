package cn.llonvne.gojudge.api.task

import arrow.core.None
import arrow.core.Option
import arrow.core.Some
import cn.llonvne.gojudge.api.spec.runtime.Cmd
import cn.llonvne.gojudge.api.spec.runtime.RequestType
import cn.llonvne.gojudge.api.spec.runtime.Result
import cn.llonvne.gojudge.api.spec.runtime.Status
import cn.llonvne.gojudge.exposed.RuntimeService
import cn.llonvne.gojudge.exposed.run
import cn.llonvne.gojudge.services.runtime.request
import com.benasher44.uuid.uuid4
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable

abstract class AbstractTask<I : Input> {
    abstract val sourceCodeExtension: Option<String>
    abstract val compiledFileExtension: Option<String>
    abstract fun getCompileCmd(input: I, filenames: Filenames): Cmd
    abstract fun getRunCmd(input: I, compileResult: Result, runFilename: Filename, runFileId: String): Cmd

    @Serializable
    sealed interface HookError<E, R> {
        @Serializable
        data class Error<E, R>(val output: E) : HookError<E, R>

        @Serializable
        data class Resume<E, R>(val result: R) : HookError<E, R>
    }

    @Serializable
    data class Filenames(val source: Filename, val compiled: Filename)

    @Serializable
    data class Filename(val name: String, @Contextual val extension: Option<String>) {
        fun asString() = when (extension) {
            None -> name
            is Some -> "$name.${extension.value}"
        }

        override fun toString(): String {
            return asString()
        }
    }

    open fun beforeAll() {
    }

    open suspend fun installDependency() {

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

    /**
     * 如果编译存在结果（不为 null）则会调用该函数
     */
    open fun hookOnCompileResult(result: Result) {}

    /**
     * 编译结果是null,调用该函数
     */
    open fun transformCompileResultNull(
        request: RequestType.Request, expectOutput: Output
    ): HookError<Output, Result> {
        return HookError.Error(expectOutput)
    }

    /**
     * 尝试改变编译状态
     */
    open fun transformCompileStatus(compileStatus: Status, compileResult: Result): Status {
        return compileStatus
    }

    /**
     * 预期的正常编译状态
     * 正常为返回 Accepted
     */
    open fun expectCompileStatus(): Status {
        return Status.Accepted
    }

    /**
     * 改变 RunFilename
     */
    open fun transformRunFilename(filename: Filename) = filename

    /**
     * 改变运行要求
     */
    open fun transformRunRequest(request: RequestType.Request): RequestType.Request {
        return request
    }

    /**
     * 改变运行结果
     */
    open fun transformRunResult(request: RequestType.Request, result: Result) = result

    /**
     * 改变运行错误的结果
     */
    open fun transformRunError(request: RequestType.Request, expect: Output): Output {
        return expect
    }

    /**
     * 改变运行成功的结果
     */
    open fun transformRunSuccess(result: Result, expectOutput: Output): Output {
        return expectOutput
    }

    /**
     * 在即将返回结果的时候做一些事
     */
    open fun hookOnReturnRunOutput(expectOutput: Output) {

    }

    sealed interface FlowOutputStatus {
        data class BeforeAll(val input: Input, val service: RuntimeService) : FlowOutputStatus

        data class BeforeCompile(val request: RequestType.Request) : FlowOutputStatus

        // 包装 Output 类型
        data class Output(val output: cn.llonvne.gojudge.api.task.Output) : FlowOutputStatus

        // 文件名
        data class FilenamesFlowOutput(val filenames: Filenames) : FlowOutputStatus

        // 完成编译
        data class CompileSuccess(val result: Result) : FlowOutputStatus

        data class RunFilename(val filename: Filename) : FlowOutputStatus

        data class RunRequest(val request: RequestType.Request) : FlowOutputStatus
    }

    /**
     * runFlow 不支持HOOK函数的形式
     */
    suspend fun runFlow(input: I, service: RuntimeService): Flow<FlowOutputStatus> = flow {

        emit(FlowOutputStatus.BeforeAll(input, service))

        val filenames = hookOnFilenames(
            Filenames(
                source = Filename(uuid4().toString(), sourceCodeExtension),
                compiled = Filename(uuid4().toString(), compiledFileExtension)
            )
        )

        emit(FlowOutputStatus.FilenamesFlowOutput(filenames))

        val compileRequest = request {
            add(getCompileCmd(input, filenames))
        }

        emit(FlowOutputStatus.BeforeCompile(compileRequest))

        val compileResult = service.run(compileRequest).getOrNull(0)
            ?: return@flow emit(FlowOutputStatus.Output(Output.Failure.CompileResultIsNull(compileRequest)))

        emit(FlowOutputStatus.CompileSuccess(compileResult))

        if (transformCompileStatus(compileResult.status, compileResult) != expectCompileStatus()) {
            return@flow emit(FlowOutputStatus.Output(Output.Failure.CompileError(compileRequest, compileResult)))
        }

        val fileId = compileResult.fileIds?.get(filenames.compiled.asString()) ?: return@flow emit(
            FlowOutputStatus.Output(
                Output.Failure.TargetFileNotExist(
                    compileRequest, compileResult
                )
            )
        )

        val runFilename = Filename(uuid4().toString(), None)
        emit(FlowOutputStatus.RunFilename(runFilename))

        val runRequest = request {
            getRunCmd(input, compileResult, runFilename, fileId)
        }
        emit(FlowOutputStatus.RunRequest(runRequest))

        val result = service.run(runRequest).firstOrNull() ?: return@flow emit(
            FlowOutputStatus.Output(
                Output.Failure.RunResultIsNull(
                    compileRequest, compileResult, runRequest
                )
            )
        )

        emit(
            FlowOutputStatus.Output(
                Output.Success(compileRequest, compileResult, runRequest, result)
            )
        )
    }

    suspend fun run(input: I, service: RuntimeService): Output {

        beforeAll()

        val filenames = hookOnFilenames(
            Filenames(
                Filename(uuid4().toString(), sourceCodeExtension), Filename(uuid4().toString(), compiledFileExtension)
            )
        )

        val compileRequest = request {
            add(getCompileCmd(input, filenames))
        }

        hookOnBeforeCompile(compileRequest)

        val compileResult = service.run(compileRequest).getOrNull(0) ?: when (val result =
            transformCompileResultNull(compileRequest, Output.Failure.CompileResultIsNull(compileRequest))) {
            is HookError.Error -> return result.output
            is HookError.Resume -> result.result
        }

        hookOnCompileResult(compileResult)

        if (transformCompileStatus(compileResult.status, compileResult) != expectCompileStatus()) {
            return Output.Failure.CompileError(compileRequest, compileResult)
        }

        val fileId =
            compileResult.fileIds?.get(filenames.compiled.asString()) ?: return Output.Failure.TargetFileNotExist(
                compileRequest, compileResult
            )

        val runFilename = transformRunFilename(Filename(uuid4().toString(), None))

        val runRequest = transformRunRequest(request {
            getRunCmd(input, compileResult, runFilename, fileId)
        })

        return when (val result = service.run(runRequest).getOrNull(0)) {
            null -> transformRunError(
                compileRequest, Output.Failure.RunResultIsNull(
                    compileRequest, compileResult, runRequest
                )
            )

            else -> transformRunSuccess(
                result, Output.Success(
                    compileRequest, compileResult, runRequest, transformRunResult(runRequest, result)
                )
            )
        }.also {
            hookOnReturnRunOutput(it)
        }
    }
}