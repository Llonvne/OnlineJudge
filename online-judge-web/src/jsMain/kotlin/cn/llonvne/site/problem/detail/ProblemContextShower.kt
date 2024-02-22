package cn.llonvne.site.problem.detail

import cn.llonvne.compoent.AlertType
import cn.llonvne.compoent.alert
import cn.llonvne.kvision.service.IProblemService.ProblemGetByIdResult.GetProblemByIdOk
import io.kvision.core.Container
import io.kvision.html.h4
import io.kvision.html.p

fun interface ProblemContextShower {
    fun show(root: Container)

    companion object {
        fun from(resp: GetProblemByIdOk): ProblemContextShower {
            return AbstractProblemContextShower(resp)
        }
    }
}

private open class AbstractProblemContextShower(private val resp: GetProblemByIdOk) : ProblemContextShower {

    protected val context = resp.problem.context

    override fun show(root: Container) {
        root.alert(AlertType.Light) {
            h4 {
                +"任务说明"
            }
            p(rich = true) {
                +context.overall
            }

            h4 {
                +"输入说明"
            }
            p {
                +context.inputDescription
            }

            h4 {
                +"输出说明"
            }
            p {
                +context.outputDescription
            }

            h4 {
                +"提示"
            }
            p {
                +context.hint
            }

            h4 {
                +"判题标准"
            }
            p {
                +context.testCases.passer.description
            }
        }
    }
}