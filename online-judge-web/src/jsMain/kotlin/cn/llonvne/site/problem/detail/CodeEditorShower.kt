package cn.llonvne.site.problem.detail

import cn.llonvne.compoent.AlertType
import cn.llonvne.compoent.alert
import cn.llonvne.compoent.submission.SubmitProblemSolutionResolver
import cn.llonvne.dtos.SubmissionSubmit
import cn.llonvne.entity.problem.SubmissionVisibilityType
import cn.llonvne.kvision.service.IProblemService.ProblemGetByIdResult.GetProblemByIdOk
import io.kvision.core.Container
import io.kvision.core.onClickLaunch
import io.kvision.form.formPanel
import io.kvision.form.select.TomSelect
import io.kvision.form.text.TextArea
import io.kvision.html.button
import io.kvision.html.h4

interface CodeEditorShower {
    fun show(root: Container)

    companion object {
        fun from(
            problemId: Int,
            getProblemByIdOk: GetProblemByIdOk
        ): CodeEditorShower {
            return AbstractCodeEditorShower(problemId, getProblemByIdOk)
        }
    }
}

private class AbstractCodeEditorShower(
    private val problemId: Int,
    getProblemByIdOk: GetProblemByIdOk
) : CodeEditorShower {

    val problem = getProblemByIdOk

    override fun show(root: Container) {
        root.alert(AlertType.Secondary) {

            h4 {
                +"你的代码"
            }

            val panel = formPanel<SubmissionSubmit> {
                add(SubmissionSubmit::languageId, TomSelect(options = problem.supportLanguages.map {
                    it.languageId.toString() to it.toString()
                }) {
                    label = "提交语言"
                })
                add(SubmissionSubmit::code, TextArea {
                    label = "解决方案"
                    rows = 10
                })
                add(SubmissionSubmit::visibilityTypeStr, TomSelect(options = SubmissionVisibilityType.entries.map {
                    it.ordinal.toString() to it.chinese
                }))
            }

            button("提交") {
                onClickLaunch {
                    SubmitProblemSolutionResolver().resolve(problemId, panel.getData())
                }
            }
        }
    }
}

