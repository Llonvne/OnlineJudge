package cn.llonvne.site.problem.detail

import cn.llonvne.compoent.AlertType
import cn.llonvne.compoent.BadgesDsl
import cn.llonvne.compoent.alert
import cn.llonvne.compoent.badges
import cn.llonvne.entity.problem.context.ProblemTestCases.ProblemTestCase
import cn.llonvne.kvision.service.IProblemService.ProblemGetByIdResult.GetProblemByIdOk
import io.kvision.core.Container
import io.kvision.html.*

fun interface TestCasesShower {
    fun load(root: Container)

    companion object {
        fun from(
            resp: GetProblemByIdOk,
            withoutTitle: Boolean = false,
            filter: (ProblemTestCase) -> Boolean = { true },
            badges: BadgesDsl.(ProblemTestCase) -> Unit = {}
        ): TestCasesShower {
            return AbstractTestCasesShower(resp.problem.context.testCases.testCases, withoutTitle, filter, badges)
        }

        fun from(
            testCase: List<ProblemTestCase>,
            withoutTitle: Boolean = false,
            filter: (ProblemTestCase) -> Boolean = { true },
            badges: BadgesDsl.(ProblemTestCase) -> Unit = {}
        ): TestCasesShower {
            return AbstractTestCasesShower(testCase, withoutTitle, filter, badges)
        }
    }
}

private open class AbstractTestCasesShower(
    private val testCase: List<ProblemTestCase>,
    private val withoutTitle: Boolean,
    private val filter: (ProblemTestCase) -> Boolean = { true },
    private val doBadges: BadgesDsl.(ProblemTestCase) -> Unit = {}
) : TestCasesShower {

    protected val testcases = testCase.filter { filter(it) }

    private fun Container.doShow() {
        testcases.forEach { testcase ->
            alert(AlertType.Primary) {
                h6 {
                    +testcase.name
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

                badges {
                    add {
                        +testcase.visibility.chinese
                    }

                    doBadges(testcase)
                }
            }
        }
    }

    private fun Container.showTitleOn(withoutTitle: Boolean, action: Container.() -> Unit) {
        if (withoutTitle) {
            action()
        } else {
            alert(AlertType.Light) {

                h4 {
                    +"输入/输出样例"
                }

                action()
            }
        }
    }

    override fun load(root: Container) {
        root.div {
            showTitleOn(withoutTitle) {
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
}