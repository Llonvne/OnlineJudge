package cn.llonvne.gojudge.api.task

import arrow.core.None
import arrow.core.Option
import arrow.core.Some
import cn.llonvne.gojudge.api.spec.Cmd
import cn.llonvne.gojudge.api.spec.RequestType
import cn.llonvne.gojudge.api.spec.Result
import cn.llonvne.gojudge.api.spec.Status
import cn.llonvne.gojudge.api.task.gpp.GppCompileTask
import cn.llonvne.gojudge.api.task.gpp.GppInput
import cn.llonvne.gojudge.exposed.RuntimeService
import cn.llonvne.gojudge.exposed.run
import cn.llonvne.gojudge.services.runtime.request
import com.benasher44.uuid.uuid4
import io.ktor.client.*
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.buffer
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

    suspend fun main1() {
        val gppTask = GppCompileTask()
        val flow = gppTask.runFlow(GppInput("", ""), RuntimeService(HttpClient { }))

        flow
            .buffer(0, BufferOverflow.SUSPEND)
            .collect {
            }
    }

    suspend fun runFlow(input: I, service: RuntimeService): Flow<Output> = flow {
        beforeAll()

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
                transformCompileResultNull(compileRequest, Output.Failure.CompileResultIsNull(compileRequest))) {
                is HookError.Error -> {
                    emit(result.output)
                    return@flow
                }

                is HookError.Resume -> {
                    result.result
                }
            }

        emit(Output.SuccessCompile(compileRequest, compileResult))

        if (transformCompileStatus(compileResult.status, compileResult) != expectCompileStatus()) {
            emit(Output.Failure.CompileError(compileRequest, compileResult))
        }

        val fileId =
            compileResult.fileIds?.get(filenames.compiled.asString())
                ?: emit(
                    Output.Failure.TargetFileNotExist(
                        compileRequest,
                        compileResult
                    )
                )
    }

    suspend fun run(input: I, service: RuntimeService): Output {

        beforeAll()

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
                transformCompileResultNull(compileRequest, Output.Failure.CompileResultIsNull(compileRequest))) {
                is HookError.Error -> return result.output
                is HookError.Resume -> result.result
            }

        hookOnCompileResult(compileResult)

        if (transformCompileStatus(compileResult.status, compileResult) != expectCompileStatus()) {
            return Output.Failure.CompileError(compileRequest, compileResult)
        }

        val fileId =
            compileResult.fileIds?.get(filenames.compiled.asString())
                ?: return Output.Failure.TargetFileNotExist(
                    compileRequest,
                    compileResult
                )

        val runFilename = transformRunFilename(Filename(uuid4().toString(), None))

        val runRequest = transformRunRequest(request {
            getRunCmd(input, compileResult, runFilename, fileId)
        })

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