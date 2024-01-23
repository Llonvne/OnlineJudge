package cn.llonvne.site

import cn.llonvne.AppScope
import cn.llonvne.compoent.AlertType
import cn.llonvne.compoent.NotFoundAble
import cn.llonvne.compoent.alert
import cn.llonvne.compoent.notFound
import cn.llonvne.dtos.CodeDto
import cn.llonvne.gojudge.api.task.Output
import cn.llonvne.gojudge.api.task.Output.Failure.*
import cn.llonvne.gojudge.api.task.Output.Success
import cn.llonvne.kvision.service.CodeNotFound
import cn.llonvne.kvision.service.ISubmissionService
import cn.llonvne.kvision.service.JudgeResultParseError
import cn.llonvne.kvision.service.PermissionDenied
import cn.llonvne.message.Messager
import cn.llonvne.model.SubmissionModel
import io.kvision.core.Container
import io.kvision.html.*
import io.kvision.utils.set
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

interface JudgeResultDisplay {

    fun load(root: Container)

    companion object {
        fun empty() = object : JudgeResultDisplay {
            override fun load(root: Container) {}
        }

        fun playground(codeId: Int, root: Container): JudgeResultDisplay = PlaygroundJudgeResultDisplay(codeId)
            .also {
                it.load(root)
            }
    }
}

private class PlaygroundJudgeResultDisplay(private val codeId: Int) : JudgeResultDisplay {

    override fun load(root: Container) {

        val result = AppScope.async {
            SubmissionModel.getJudgeResultByCodeID(codeId)
        }
        root.div {


            AppScope.launch {
                result.await().let { resp ->
                    when (resp) {
                        CodeNotFound -> notFound(object : NotFoundAble {
                            override val header: String
                                get() = "评测结果未找到"
                            override val notice: String
                                get() = "请尝试再次提交，如果还是错误，请联系我们"
                            override val errorCode: String
                                get() = "ErrorCode-PlaygroundOutputNotFound-CodeId-${codeId}"

                        })

                        JudgeResultParseError -> notFound(object : NotFoundAble {
                            override val header: String
                                get() = "无法解析评测结果"
                            override val notice: String
                                get() = "请尝试再次提交，如果还是错误，请联系我们"
                            override val errorCode: String
                                get() = "JudgeResultParseError-CodeId-${codeId}"

                        })

                        PermissionDenied -> Messager.toastInfo("请登入来查看对应评测结果")

                        ISubmissionService.SubmissionNotFound -> notFound(object : NotFoundAble {
                            override val header: String
                                get() = "找到不到提交记录"
                            override val notice: String
                                get() = "请确认提交号正确"
                            override val errorCode: String
                                get() = "SubmissionNotFound-CodeId-${codeId}"
                        })

                        is ISubmissionService.GetOutputByCodeIdResp.SuccessGetOutput -> {
                            when (resp.output) {
                                is CompileError -> {
                                    alert(AlertType.Danger) {
                                        h3 {
                                            +"编译错误"
                                        }

                                        p {
                                            +resp.output.compileResult.files?.get("stderr").toString()
                                        }
                                    }
                                }

                                is CompileResultIsNull -> {
                                    alert(AlertType.Danger) {
                                        h3 {
                                            +"未找到编译结果"
                                        }

                                        p {
                                            +"这应该不是您的问题，请尝试重新提交"
                                        }
                                    }
                                }

                                is RunResultIsNull -> {
                                    alert(AlertType.Danger) {
                                        h3 {
                                            +"未找到运行结果"
                                        }

                                        p {
                                            +"这应该不是您的问题，请尝试重新提交"
                                        }
                                    }
                                }

                                is TargetFileNotExist -> {
                                    alert(AlertType.Danger) {
                                        h3 {
                                            +"未找到运行目标"
                                        }

                                        p {
                                            +"这应该不是您的问题，请尝试重新提交"
                                        }
                                    }
                                }

                                is Success -> {
                                    alert(AlertType.Success) {
                                        h4 {
                                            +"运行成功"
                                        }
                                        label {
                                            +"标准输入"
                                        }
                                        p {
                                            customTag("pre") {
                                                code {
                                                    +(resp.output.runResult.files?.get("stdin") ?: "<Null>")
                                                }
                                            }
                                        }
                                        label {
                                            +"标准输出"
                                        }
                                        p {
                                            customTag("pre") {
                                                code {
                                                    +(resp.output.runResult.files?.get("stdout") ?: "<Null>")
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}