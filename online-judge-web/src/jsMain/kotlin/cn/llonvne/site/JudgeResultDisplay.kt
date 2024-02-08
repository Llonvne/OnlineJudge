package cn.llonvne.site

import cn.llonvne.AppScope
import cn.llonvne.compoent.AlertType
import cn.llonvne.compoent.alert
import cn.llonvne.compoent.badge
import cn.llonvne.entity.types.badge.BadgeColor
import cn.llonvne.kvision.service.*
import cn.llonvne.kvision.service.ISubmissionService.GetOutputByCodeIdResp.OutputDto
import cn.llonvne.kvision.service.ISubmissionService.GetOutputByCodeIdResp.OutputDto.FailureReason.*
import cn.llonvne.kvision.service.ISubmissionService.GetOutputByCodeIdResp.OutputDto.SuccessOutput
import cn.llonvne.kvision.service.ISubmissionService.GetOutputByCodeIdResp.SuccessGetOutput
import cn.llonvne.message.Messager
import cn.llonvne.model.SubmissionModel
import io.kvision.core.Container
import io.kvision.html.*
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

interface JudgeResultDisplay {

    fun load(root: Container)

    fun display(root: Container, outputDto: OutputDto)

    fun onCompilerError(root: Container, compileError: CompileError)

    fun onCompileResultNotFound(root: Container, compileResultNotFound: CompileResultNotFound)

    fun onRunResultIsNull(root: Container, runResultIsNull: RunResultIsNull)

    fun onTargetResultNotFound(root: Container, targetFileNotExist: TargetResultNotFound)

    fun onSuccess(root: Container, successOutput: SuccessOutput)

    companion object {
        fun empty() = object : JudgeResultDisplay {
            override fun load(root: Container) {}
            override fun display(root: Container, outputDto: OutputDto) {}
            override fun onCompilerError(root: Container, compileError: CompileError) {}
            override fun onCompileResultNotFound(root: Container, compileResultNotFound: CompileResultNotFound) {}
            override fun onRunResultIsNull(root: Container, runResultIsNull: RunResultIsNull) {}
            override fun onTargetResultNotFound(root: Container, targetFileNotExist: TargetResultNotFound) {}
            override fun onSuccess(root: Container, successOutput: SuccessOutput) {}
        }

        fun playground(codeId: Int, root: Container): JudgeResultDisplay = PlaygroundJudgeResultDisplay(codeId)
            .also {
                it.load(root)
            }
    }
}

private class PlaygroundJudgeResultDisplay(
    private val codeId: Int,
    private val errorHandler: ErrorHandler<Int> = JudgeResultDisplayErrorHandler.getHandler(),
) : JudgeResultDisplay {

    override fun load(root: Container) {

        val result = AppScope.async {
            SubmissionModel.getJudgeResultByCodeID(codeId)
        }
        root.div {
            AppScope.launch {
                result.await().let { resp ->
                    when (resp) {
                        LanguageNotFound -> errorHandler.handleLanguageNotFound(this@div, codeId)
                        CodeNotFound -> errorHandler.handleCodeNotFound(this@div, codeId)
                        JudgeResultParseError -> errorHandler.handleJudgeResultParseError(this@div, codeId)
                        PermissionDenied -> Messager.toastInfo("请登入来查看对应评测结果")
                        ISubmissionService.SubmissionNotFound -> errorHandler.handleSubmissionNotFound(this@div, codeId)
                        is SuccessGetOutput -> display(this@div, resp.outputDto)
                    }
                }
            }
        }
    }

    override fun onCompilerError(root: Container, compileError: CompileError) {
        root.alert(AlertType.Danger) {
            h3 {
                +"编译错误"
            }

            p {
                +compileError.compileErrMessage
            }
        }
    }

    override fun onCompileResultNotFound(root: Container, compileResultNotFound: CompileResultNotFound) {
        root.alert(AlertType.Danger) {
            h3 {
                +"未找到编译结果"
            }

            p {
                +"这应该不是您的问题，请尝试重新提交"
            }
        }
    }

    override fun onRunResultIsNull(root: Container, runResultIsNull: RunResultIsNull) {
        root.alert(AlertType.Danger) {
            h3 {
                +"未找到运行结果"
            }

            p {
                +"这应该不是您的问题，请尝试重新提交"
            }
        }
    }

    override fun onTargetResultNotFound(root: Container, targetFileNotExist: TargetResultNotFound) {
        root.alert(AlertType.Danger) {
            h3 {
                +"未找到运行目标"
            }

            p {
                +"这应该不是您的问题，请尝试重新提交"
            }
        }
    }

    override fun onSuccess(root: Container, successOutput: SuccessOutput) {
        root.alert(AlertType.Success) {
            h4 {
                +"运行成功"
            }

            badge(BadgeColor.Blue) {
            }

            label {
                +"标准输入"
            }
            p {
                customTag("pre") {
                    code {
                        +successOutput.stdin
                    }
                }
            }
            label {
                +"标准输出"
            }
            p {
                customTag("pre") {
                    code {
                        +successOutput.stdout
                    }
                }
            }
        }
    }

    override fun display(root: Container, outputDto: OutputDto) {
        when (outputDto) {
            is OutputDto.FailureOutput -> {
                when (val reason = outputDto.reason) {
                    is CompileError -> onCompilerError(root, reason)
                    is CompileResultNotFound -> onCompileResultNotFound(root, reason)
                    is RunResultIsNull -> onRunResultIsNull(root, reason)
                    is TargetResultNotFound -> onTargetResultNotFound(root, reason)
                }
            }

            is SuccessOutput -> onSuccess(root, outputDto)
        }
    }
}