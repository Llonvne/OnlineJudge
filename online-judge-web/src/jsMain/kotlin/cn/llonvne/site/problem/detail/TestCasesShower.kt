package cn.llonvne.site.problem.detail

import cn.llonvne.compoent.AlertType
import cn.llonvne.compoent.alert
import cn.llonvne.kvision.service.IProblemService
import cn.llonvne.kvision.service.IProblemService.ProblemGetByIdResult.GetProblemByIdOk
import io.kvision.core.Container
import io.kvision.html.*

fun interface TestCasesShower {
    fun load(root: Container)

    companion object {
        fun from(resp: GetProblemByIdOk): TestCasesShower {
            return AbstractTestCasesShower(resp)
        }
    }
}

private open class AbstractTestCasesShower(private val resp: GetProblemByIdOk) : TestCasesShower {

    protected val testcases = resp.problem.context.testCases.canShow()

    private fun Container.doShow() {
        testcases.forEach { testcase ->
            alert(AlertType.Secondary) {
                p {
                    +"样例名称${testcase.name}"
                }

                label { +"输入" }
                customTag("pre") {
                    code {
                        +testcase.input
                    }
                }

                label { +"输出" }
                customTag("pre") {
                    code {
                        +testcase.output
                    }
                }
            }
        }
    }

    override fun load(root: Container) {
        root.alert(AlertType.Light) {

            h4 {
                +"输入/输出样例"
            }

            if (testcases.isEmpty()) {
                p {
                    +"无输入/输出样例"
                }
            } else {
                doShow()
            }
        }
    }
}