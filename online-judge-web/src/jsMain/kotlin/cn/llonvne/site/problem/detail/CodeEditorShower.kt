package cn.llonvne.site.problem.detail

import cn.llonvne.compoent.AlertType
import cn.llonvne.compoent.alert
import cn.llonvne.compoent.submission.SubmitProblemResolver
import cn.llonvne.dtos.PlaygroudSubmission
import cn.llonvne.entity.problem.SubmissionVisibilityType
import cn.llonvne.kvision.service.IProblemService.ProblemGetByIdResult.GetProblemByIdOk
import cn.llonvne.site.problem.detail.CodeEditorShower.Companion.CodeEditorConfigurer
import io.kvision.core.Container
import io.kvision.core.onClickLaunch
import io.kvision.form.formPanel
import io.kvision.form.select.TomSelect
import io.kvision.form.text.TextArea
import io.kvision.html.button
import io.kvision.html.h4
import io.kvision.html.p

interface CodeEditorShower {
    fun show(root: Container)

    companion object {
        fun from(
            problemId: Int,
            getProblemByIdOk: GetProblemByIdOk,
            codeEditorConfigurer: CodeEditorConfigurer
        ): CodeEditorShower {
            return AbstractCodeEditorShower(problemId, getProblemByIdOk, codeEditorConfigurer)
        }

        class CodeEditorConfigurer {
            /**
             * 设置为 null 允许用户进行选择，否则将使用这里设定的可见性设置
             */
            var forceVisibility: SubmissionVisibilityType? = null

            var submitProblemResolver = SubmitProblemResolver()

            var showSubmitPanel: Boolean = true

            var notShowSubmitPanelMessage: String = ""
        }

        fun CodeEditorConfigurer(action: CodeEditorConfigurer.() -> Unit): CodeEditorConfigurer {
            val codeEditorConfigurer = CodeEditorConfigurer()
            codeEditorConfigurer.action()
            return codeEditorConfigurer
        }
    }
}

private class AbstractCodeEditorShower(
    private val problemId: Int,
    getProblemByIdOk: GetProblemByIdOk,
    private val codeEditorConfigurer: CodeEditorConfigurer
) : CodeEditorShower {

    val problem = getProblemByIdOk

    override fun show(root: Container) {
        if (codeEditorConfigurer.showSubmitPanel) {
            doShow(root)
        } else {
            root.alert(AlertType.Secondary) {
                h4 {
                    +"提交面板已经被关闭"
                }

                p {
                    +codeEditorConfigurer.notShowSubmitPanelMessage
                }
            }
        }
    }

    private fun doShow(root: Container) {
        root.alert(AlertType.Secondary) {

            h4 {
                +"你的代码"
            }

            val panel = formPanel<PlaygroudSubmission> {
                add(PlaygroudSubmission::languageId, TomSelect(options = problem.supportLanguages.map {
                    it.languageId.toString() to it.toString()
                }) {
                    label = "提交语言"
                })
                add(PlaygroudSubmission::code, TextArea {
                    label = "解决方案"
                    rows = 10
                })

                if (
                    codeEditorConfigurer.forceVisibility == null
                ) {
                    add(PlaygroudSubmission::visibilityTypeStr, TomSelect(options = SubmissionVisibilityType.entries.map {
                        it.ordinal.toString() to it.chinese
                    }, label = "提交可见性"))
                }
            }

            button("提交") {
                onClickLaunch {
                    codeEditorConfigurer.submitProblemResolver.resolve(problemId, panel.getData().let {
                        if (codeEditorConfigurer.forceVisibility != null) {
                            it.copy(visibilityTypeStr = codeEditorConfigurer.forceVisibility?.ordinal.toString())
                        } else {
                            it
                        }
                    })
                }
            }
        }
    }
}

