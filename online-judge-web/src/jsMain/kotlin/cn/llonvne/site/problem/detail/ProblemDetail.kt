package cn.llonvne.site.problem.detail

import cn.llonvne.compoent.*
import cn.llonvne.compoent.observable.observableOf
import cn.llonvne.entity.problem.context.TestCaseType
import cn.llonvne.kvision.service.IProblemService.ProblemGetByIdResult
import cn.llonvne.kvision.service.IProblemService.ProblemGetByIdResult.GetProblemByIdOk
import cn.llonvne.kvision.service.IProblemService.ProblemGetByIdResult.ProblemNotFound
import cn.llonvne.model.ProblemModel
import io.kvision.core.Container
import io.kvision.html.div
import io.kvision.html.h4
import io.kvision.html.p

fun detail(
    root: Container,
    problemId: Int,
    configurer: ProblemDetailConfigurer.() -> Unit = {},
) {
    val configure = ProblemDetailConfigurer()
    configure.configurer()

    if (configure.notShowProblem) {
        root.alert(AlertType.Secondary) {
            h4 {
                +"题目显示已经被关闭"
            }

            p {
                +configure.notShowProblemMessage
            }
        }
        return
    }

    observableOf<ProblemGetByIdResult>(null) {
        setUpdater {
            ProblemModel.getById(problemId)
        }

        sync(root.div { }) { resp ->
            if (resp == null) {
                loading()
            } else {
                ProblemDetailShower.from(problemId, resp, configure).show(this)
            }
        }
    }
}

private fun interface ProblemDetailShower {
    fun show(root: Container)

    companion object {
        private fun problemNotFoundDetailShower(problemId: Int) =
            ProblemDetailShower { root ->
                root.notFound(
                    object : NotFoundAble {
                        override val header: String
                            get() = "题目未找到"
                        override val notice: String
                            get() = "请确认题目ID正确，如果确认题目ID正确，请联系我们 ^_^"
                        override val errorCode: String = "ProblemNotFound-$problemId"
                    },
                )
            }

        fun from(
            problemId: Int,
            resp: ProblemGetByIdResult,
            configure: ProblemDetailConfigurer,
        ): ProblemDetailShower =
            when (resp) {
                is GetProblemByIdOk -> AbstractProblemDetailShower(problemId, resp, configure)
                ProblemNotFound -> problemNotFoundDetailShower(problemId = problemId)
            }
    }
}

private open class AbstractProblemDetailShower(
    private val problemId: Int,
    resp: GetProblemByIdOk,
    private val configure: ProblemDetailConfigurer,
) : ProblemDetailShower {
    private val headerShower = DetailHeaderShower.from(resp)

    private val contextShower = ProblemContextShower.from(resp)

    private val codeEditorShower =
        CodeEditorShower.from(
            problemId,
            resp,
            configure.codeEditorConfigurer,
        )

    private val testCasesShower =
        TestCasesShower.from(resp, filter = {
            it.visibility in setOf(TestCaseType.ViewAndJudge, TestCaseType.OnlyForView)
        })

    private val submissionsShower = ProblemSubmissionShower.from(resp)

    override fun show(root: Container) {
        root.div {
            headerShower.show(div { })

            div(className = "row") {
                div(className = "col") {
                    contextShower.show(div { })

                    testCasesShower.load(div { })
                }
                div(className = "col") {
                    codeEditorShower.show(div { })

                    if (!configure.disableHistory) {
                        submissionsShower.show(div { })
                    }
                }
            }
        }
    }
}
