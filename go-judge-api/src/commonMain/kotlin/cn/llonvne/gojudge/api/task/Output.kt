package cn.llonvne.gojudge.api.task

import cn.llonvne.gojudge.api.spec.RequestType
import cn.llonvne.gojudge.api.spec.Result

sealed interface Output {

    sealed interface Failure : Output {
        data class CompileResultIsNull(val compileRequest: RequestType.Request) : Failure

        data class CompileError(val compileRequest: RequestType.Request, val compileResult: Result) : Failure

        data class TargetFileNotExist(val compileRequest: RequestType.Request, val compileResult: Result) : Failure

        data class RunResultIsNull(
            val compileRequest: RequestType.Request,
            val compileResult: Result,
            val runRequest: RequestType.Request
        ) : Failure
    }

    data class Success(
        val compileRequest: RequestType.Request,
        val compileResult: Result,
        val runRequest: RequestType.Request,
        val runResult: Result
    ) : Output

    data class SuccessWithoutCompile(
        val runRequest: RequestType.Request,
        val runResult: Result
    ) : Output

    data class SuccessCompile(
        val compileRequest: RequestType.Request,
        val compileResult: Result,
    ) : Output
}