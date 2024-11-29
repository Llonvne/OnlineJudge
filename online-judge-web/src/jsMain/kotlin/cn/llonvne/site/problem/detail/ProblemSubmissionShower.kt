package cn.llonvne.site.problem.detail

import cn.llonvne.compoent.AlertType
import cn.llonvne.compoent.alert
import cn.llonvne.compoent.defineColumn
import cn.llonvne.compoent.loading
import cn.llonvne.compoent.observable.observableOf
import cn.llonvne.entity.problem.context.passer.PasserResult
import cn.llonvne.kvision.service.IProblemService.ProblemGetByIdResult.GetProblemByIdOk
import cn.llonvne.kvision.service.ISubmissionService
import cn.llonvne.kvision.service.ISubmissionService.GetLastNProblemSubmissionResp.GetLastNProblemSubmissionRespImpl
import cn.llonvne.kvision.service.LanguageNotFound
import cn.llonvne.kvision.service.PermissionDenied
import cn.llonvne.ll
import cn.llonvne.message.Messager
import cn.llonvne.model.SubmissionModel
import cn.llonvne.site.BooleanPasserResultDisplay
import cn.llonvne.site.JudgeResultDisplayErrorHandler
import io.kvision.core.Container
import io.kvision.html.Span
import io.kvision.html.h4
import io.kvision.tabulator.Layout
import io.kvision.tabulator.TabulatorOptions
import io.kvision.tabulator.tabulator

interface ProblemSubmissionShower {
    fun show(div: Container)

    companion object {
        fun from(resp: GetProblemByIdOk): ProblemSubmissionShower = BaseProblemSubmissionShower(resp)
    }
}

private class BaseProblemSubmissionShower(
    private val resp: GetProblemByIdOk,
) : ProblemSubmissionShower {
    private val errorHandler = JudgeResultDisplayErrorHandler.getHandler()

    override fun show(div: Container) {
        if (resp.problem.problemId == null) {
            return Messager.toastInfo("ProblemId 为空")
        }

        observableOf<ISubmissionService.GetLastNProblemSubmissionResp>(null) {
            setUpdater {
                SubmissionModel.getLastNProblemSubmission(resp.problem.problemId)
            }
            div.sync { resp ->
                if (resp == null) {
                    loading()
                } else {
                    onShow(this, resp)
                }
            }
        }
    }

    private fun onShow(
        root: Container,
        resp: ISubmissionService.GetLastNProblemSubmissionResp,
    ) {
        when (resp) {
            is GetLastNProblemSubmissionRespImpl -> onSuccess(root, resp)
            LanguageNotFound -> errorHandler.handleLanguageNotFound(root, -1)
            PermissionDenied -> Messager.toastInfo("你还为登入哦，无法查看历史记录")
            ISubmissionService.ProblemNotFound -> Messager.toastInfo("该题目不存在，或者已被删除")
        }
    }

    private fun onSuccess(
        root: Container,
        resp: GetLastNProblemSubmissionRespImpl,
    ) {
        console.log(resp.submissions)

        root.alert(AlertType.Light) {
            h4 {
                +"提交记录"
            }

            tabulator(
                resp.submissions,
                options =
                    TabulatorOptions(
                        layout = Layout.FITCOLUMNS,
                        columns =
                            listOf(
                                defineColumn("提交时间") {
                                    Span {
                                        +it.submitTime.ll()
                                    }
                                },
                                defineColumn("提交语言") {
                                    Span {
                                        +(it.language.languageName + ":" + it.language.languageVersion)
                                    }
                                },
                                defineColumn("结果") {
                                    when (it.passerResult) {
                                        is PasserResult.BooleanResult -> {
                                            Span {
                                                BooleanPasserResultDisplay(it.passerResult, codeId = it.codeId, small = true)
                                                    .load(this)
                                            }
                                        }
                                    }
                                },
                            ),
                    ),
            )
        }
    }
}
