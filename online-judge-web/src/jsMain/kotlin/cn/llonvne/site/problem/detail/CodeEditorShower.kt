package cn.llonvne.site.problem.detail

import cn.llonvne.compoent.AlertType
import cn.llonvne.compoent.alert
import cn.llonvne.entity.problem.context.Problem
import cn.llonvne.kvision.service.IProblemService
import cn.llonvne.kvision.service.IProblemService.ProblemGetByIdResult.GetProblemByIdOk
import cn.llonvne.site.problem.detail.CodeEditorShower.Companion.SubmissionSubmit
import io.kvision.core.Container
import io.kvision.form.formPanel
import io.kvision.form.select.TomSelect
import io.kvision.form.text.textArea
import io.kvision.html.h4
import kotlinx.serialization.Serializable

interface CodeEditorShower {
    fun show(root: Container)

    companion object {
        fun from(getProblemByIdOk: GetProblemByIdOk): CodeEditorShower {
            return AbstractCodeEditorShower(getProblemByIdOk)
        }

        @Serializable
        data class SubmissionSubmit(
            val languageId: String?,
            val code: String?,
            val visibilityTypeStr: String
        )
    }
}

private class AbstractCodeEditorShower(getProblemByIdOk: GetProblemByIdOk) : CodeEditorShower {

    val problem = getProblemByIdOk

    override fun show(root: Container) {
        root.alert(AlertType.Secondary) {

            h4 {
                +"你的代码"
            }

            formPanel<SubmissionSubmit> {
                add(SubmissionSubmit::languageId, TomSelect(options = problem.supportLanguages.map {
                    it.languageId.toString() to it.toString()
                }) {
                    label = "提交语言"
                })
            }

            textArea {
                rows = 10
            }
        }
    }
}

