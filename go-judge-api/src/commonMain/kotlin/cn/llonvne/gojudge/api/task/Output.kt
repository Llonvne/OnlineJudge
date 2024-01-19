package cn.llonvne.gojudge.api.task

import cn.llonvne.gojudge.api.spec.runtime.RequestType
import cn.llonvne.gojudge.api.spec.runtime.Result
import kotlinx.serialization.Serializable

@Serializable
sealed interface Output {
    @Serializable
    sealed interface Failure : Output {
        @Serializable
        data class CompileResultIsNull(val compileRequest: RequestType.Request) : Failure

        @Serializable
        data class CompileError(val compileRequest: RequestType.Request, val compileResult: Result) : Failure

        @Serializable
        data class TargetFileNotExist(val compileRequest: RequestType.Request, val compileResult: Result) : Failure

        @Serializable
        data class RunResultIsNull(
            val compileRequest: RequestType.Request,
            val compileResult: Result,
            val runRequest: RequestType.Request
        ) : Failure
    }

    @Serializable
    data class Success(
        val compileRequest: RequestType.Request,
        val compileResult: Result,
        val runRequest: RequestType.Request,
        val runResult: Result
    ) : Output

    companion object {
        fun Output.formatOnSuccess(notSuccess: String, onSuccess: (Success) -> String): String {
            return when (this) {
                is Success -> onSuccess(this)
                else -> notSuccess
            }
        }
    }
}