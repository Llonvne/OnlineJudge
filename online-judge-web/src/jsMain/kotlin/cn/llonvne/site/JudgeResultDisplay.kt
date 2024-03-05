package cn.llonvne.site

import cn.llonvne.compoent.AlertType
import cn.llonvne.compoent.alert
import cn.llonvne.compoent.badge
import cn.llonvne.compoent.badges
import cn.llonvne.compoent.observable.observableOf
import cn.llonvne.entity.problem.ProblemJudgeResult
import cn.llonvne.entity.types.badge.BadgeColor
import cn.llonvne.kvision.service.CodeNotFound
import cn.llonvne.kvision.service.ISubmissionService.GetJudgeResultByCodeIdResp
import cn.llonvne.kvision.service.ISubmissionService.PlaygroundOutput.OutputDto
import cn.llonvne.kvision.service.ISubmissionService.PlaygroundOutput.OutputDto.FailureReason.*
import cn.llonvne.kvision.service.ISubmissionService.PlaygroundOutput.OutputDto.SuccessOutput
import cn.llonvne.kvision.service.ISubmissionService.PlaygroundOutput.SuccessPlaygroundOutput
import cn.llonvne.kvision.service.ISubmissionService.ProblemOutput.SuccessProblemOutput
import cn.llonvne.kvision.service.ISubmissionService.SubmissionNotFound
import cn.llonvne.kvision.service.JudgeResultParseError
import cn.llonvne.kvision.service.LanguageNotFound
import cn.llonvne.kvision.service.PermissionDenied
import cn.llonvne.message.Messager
import cn.llonvne.model.SubmissionModel
import io.kvision.core.Container
import io.kvision.html.*

interface JudgeResultDisplay {

    fun load(root: Container)

    companion object {
        fun empty() = object : JudgeResultDisplay {
            override fun load(root: Container) {}
        }

        fun playground(codeId: Int, root: Container): JudgeResultDisplay = PlaygroundJudgeResultDisplay(codeId)
            .also { it.load(root) }

        fun problem(codeId: Int, div: Div): JudgeResultDisplay = ProblemJudgeResultDisplay(codeId)
            .also { it.load(root = div) }
    }
}

private class ProblemJudgeResultDisplay(
    private val codeId: Int,
    private val errorHandler: ErrorHandler<Int> = JudgeResultDisplayErrorHandler.getHandler()
) : JudgeResultDisplay {
    override fun load(root: Container) {
        observableOf<GetJudgeResultByCodeIdResp>(null) {
            setUpdater { SubmissionModel.getJudgeResultByCodeID(codeId) }

            root.syncNotNull { resp ->
                when (resp) {
                    LanguageNotFound -> errorHandler.handleLanguageNotFound(this, codeId)
                    CodeNotFound -> errorHandler.handleCodeNotFound(this, codeId)
                    JudgeResultParseError -> errorHandler.handleJudgeResultParseError(this, codeId)
                    PermissionDenied -> Messager.toastInfo("请登入来查看对应评测结果")
                    SubmissionNotFound -> errorHandler.handleSubmissionNotFound(this, codeId)
                    is SuccessPlaygroundOutput -> error("这不应该发生")
                    is SuccessProblemOutput -> display(root, resp.problem)
                }
            }
        }
    }

    private fun display(root: Container, judgeResult: ProblemJudgeResult) {

        root.alert(AlertType.Dark) {

            PasserResultDisplay.from(judgeResult.passerResult).load(root)

            h4 {
                +"测评详细结果"
            }

            p {
                +"本处仅展示部分公开的测试用例"

                addCssClass("small")
            }

            judgeResult.submissionTestCases.showOnJudgeResultDisplay
                .forEach { testcase ->
                    alert(AlertType.Light) {
                        div {
                            p {
                                +"输入:"
                                customTag("pre") {
                                    customTag("code") {
                                        +testcase.input
                                    }
                                }

                                +"期望输出:"
                                customTag("pre") {
                                    customTag("code") {
                                        +testcase.expect
                                    }
                                }
                                +"实际输出:"
                                customTag("pre") {
                                    customTag("code") {
                                        +(testcase.outputStr ?: "<Null>")
                                    }
                                }
                            }
                        }
                        badges {
                            if (testcase.outputStr?.trimIndent() == testcase.expect) {
                                add(BadgeColor.Green) {
                                    +"通过"
                                }
                            } else {
                                add(BadgeColor.Red) {
                                    +"失败"
                                }
                            }
                        }
                    }
                }
        }
    }
}


private class PlaygroundJudgeResultDisplay(
    private val codeId: Int,
    private val errorHandler: ErrorHandler<Int> = JudgeResultDisplayErrorHandler.getHandler(),
) : JudgeResultDisplay {

    override fun load(root: Container) {
        observableOf<GetJudgeResultByCodeIdResp>(null) {
            setUpdater { SubmissionModel.getJudgeResultByCodeID(codeId) }

            root.syncNotNull { resp ->
                when (resp) {
                    LanguageNotFound -> errorHandler.handleLanguageNotFound(this, codeId)
                    CodeNotFound -> errorHandler.handleCodeNotFound(this, codeId)
                    JudgeResultParseError -> errorHandler.handleJudgeResultParseError(this, codeId)
                    PermissionDenied -> Messager.toastInfo("请登入来查看对应评测结果")
                    SubmissionNotFound -> errorHandler.handleSubmissionNotFound(this, codeId)
                    is SuccessPlaygroundOutput -> display(this, resp.outputDto)
                    is SuccessProblemOutput -> error("这不应该发生")
                }
            }
        }
    }

    fun onCompilerError(root: Container, compileError: CompileError) {
        root.alert(AlertType.Danger) {
            h3 {
                +"编译错误"
            }

            p {
                +compileError.compileErrMessage
            }
        }
    }

    fun onCompileResultNotFound(root: Container, compileResultNotFound: CompileResultNotFound) {
        root.alert(AlertType.Danger) {
            h3 {
                +"未找到编译结果"
            }

            p {
                +"这应该不是您的问题，请尝试重新提交"
            }
        }
    }

    fun onRunResultIsNull(root: Container, runResultIsNull: RunResultIsNull) {
        root.alert(AlertType.Danger) {
            h3 {
                +"未找到运行结果"
            }

            p {
                +"这应该不是您的问题，请尝试重新提交"
            }
        }
    }

    fun onTargetResultNotFound(root: Container, targetFileNotExist: TargetResultNotFound) {
        root.alert(AlertType.Danger) {
            h3 {
                +"未找到运行目标"
            }

            p {
                +"这应该不是您的问题，请尝试重新提交"
            }
        }
    }

    fun onSuccess(root: Container, successOutput: SuccessOutput) {
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

    fun display(root: Container, outputDto: OutputDto) {
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