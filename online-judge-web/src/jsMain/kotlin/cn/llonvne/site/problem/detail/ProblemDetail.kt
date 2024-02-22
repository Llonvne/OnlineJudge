package cn.llonvne.site.problem.detail

import cn.llonvne.compoent.*
import cn.llonvne.compoent.observable.observableOf
import cn.llonvne.compoent.observable.observableProblemOf
import cn.llonvne.kvision.service.IProblemService.ProblemGetByIdResult
import cn.llonvne.kvision.service.IProblemService.ProblemGetByIdResult.GetProblemByIdOk
import cn.llonvne.kvision.service.IProblemService.ProblemGetByIdResult.ProblemNotFound
import cn.llonvne.ll
import cn.llonvne.model.ProblemModel
import io.kvision.core.*
import io.kvision.html.*
import io.kvision.routing.Routing
import io.kvision.state.bind
import kotlinx.serialization.Serializable

fun detail(root: Container, problemId: Int) {
    observableOf<ProblemGetByIdResult>(null) {
        setUpdater {
            ProblemModel.getById(problemId)
        }

        sync(root.div { }) { resp ->
            if (resp == null) {
                loading()
            } else {
                ProblemDetailShower.from(problemId, resp).show(this)
            }
        }
    }
}

private fun interface ProblemDetailShower {
    fun show(root: Container)

    companion object {

        private fun problemNotFoundDetailShower(problemId: Int) = ProblemDetailShower { root ->
            root.notFound(object : NotFoundAble {
                override val header: String
                    get() = "题目未找到"
                override val notice: String
                    get() = "请确认题目ID正确，如果确认题目ID正确，请联系我们 ^_^"
                override val errorCode: String = "ProblemNotFound-$problemId"
            })
        }

        fun from(problemId: Int, resp: ProblemGetByIdResult): ProblemDetailShower {
            return when (resp) {
                is GetProblemByIdOk -> AbstractProblemDetailShower(resp)
                ProblemNotFound -> problemNotFoundDetailShower(problemId = problemId)
            }
        }
    }
}

private open class AbstractProblemDetailShower(resp: GetProblemByIdOk) : ProblemDetailShower {

    private val headerShower = DetailHeaderShower.from(resp)

    private val contextShower = ProblemContextShower.from(resp)

    private val codeEditorShower = CodeEditorShower.from(resp)

    override fun show(root: Container) {
        root.div {
            headerShower.show(div { })

            div(className = "row") {
                div(className = "col") {
                    contextShower.show(div { })
                }
                div(className = "col") {
                    codeEditorShower.show(div { })
                }
            }
        }

    }
}



