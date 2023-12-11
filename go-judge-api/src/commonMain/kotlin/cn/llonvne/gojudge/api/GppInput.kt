package cn.llonvne.gojudge.api

data class GppInput(val code: String, val stdin: String)

sealed interface GppOutput {

    sealed interface Failure : GppOutput {
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
    ) : GppOutput
}