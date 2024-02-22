package cn.llonvne.site.problem

import cn.llonvne.compoent.AlertType
import cn.llonvne.compoent.alert
import cn.llonvne.compoent.observable.observableOf
import cn.llonvne.entity.problem.context.ProblemTestCases
import cn.llonvne.entity.problem.context.ProblemTestCases.ProblemTestCase
import cn.llonvne.entity.problem.context.ProblemType
import cn.llonvne.entity.problem.context.ProblemVisibility
import cn.llonvne.entity.problem.context.TestCaseType
import cn.llonvne.entity.problem.context.passer.ProblemPasser
import cn.llonvne.gojudge.api.SupportLanguages
import cn.llonvne.message.Messager
import cn.llonvne.model.AuthenticationModel
import cn.llonvne.model.ProblemModel
import cn.llonvne.site.problem.detail.TestCasesShower
import io.kvision.core.Container
import io.kvision.core.onClick
import io.kvision.core.onClickLaunch
import io.kvision.form.formPanel
import io.kvision.form.number.Numeric
import io.kvision.form.select.TomSelect
import io.kvision.form.text.RichText
import io.kvision.form.text.Text
import io.kvision.html.*
import io.kvision.routing.Routing
import io.kvision.state.bind
import io.kvision.utils.px
import kotlinx.serialization.Serializable

@Serializable
data class CreateProblemForm(
    val problemName: String,
    val problemDescription: String,
    val timeLimit: Long,
    val memoryLimit: Long,
    val authorId: Int,
    val problemVisibilityInt: String,
    val problemTypeInt: String,
    val overall: String,
    val inputDescr: String,
    val outputDescr: String,
    val hint: String,
    val problemSupportLanguages: String,
)

@Serializable
data class ProblemTestCaseForm(
    val name: String,
    val input: String,
    val output: String,
    val visibilityStr: String,
)

fun Container.createProblem(routing: Routing) {

    alert(AlertType.Success) {
        h1 {
            +"创建题目"
        }

        p {
            +"创建属于您自己的题目以供训练"
        }
    }

    val testCases = ProblemTestCases(
        listOf(), ProblemPasser.PassAllCases
    )

    observableOf(ProblemTestCases(listOf(), ProblemPasser.PassAllCases)) {
        div(className = "row") {
            div(className = "col") {
                alert(AlertType.Light) {
                    val panel = formPanel {
                        add(CreateProblemForm::problemName, Text(label = "题目名字"))
                        add(CreateProblemForm::problemDescription, Text(label = "题目描述"))
                        add(CreateProblemForm::timeLimit, Numeric(min = 0, max = 1_0000_0000, label = "时间限制"))
                        add(CreateProblemForm::memoryLimit, Numeric(min = 0, max = 1_0000_0000, label = "内存限制"))
                        add(CreateProblemForm::authorId, Numeric(min = 0, label = "作者ID"))
                        add(CreateProblemForm::overall, RichText(label = "题目要求"))
                        add(CreateProblemForm::inputDescr, Text(label = "输入描述"))
                        add(CreateProblemForm::outputDescr, Text(label = "输出描述"))
                        add(CreateProblemForm::hint, Text(label = "题目提示"))
                        add(
                            CreateProblemForm::problemVisibilityInt, TomSelect(
                                options = ProblemVisibility.entries.map {
                                    it.ordinal.toString() to it.chinese
                                },
                                label = "题目可见性设置"
                            )
                        )
                        add(
                            CreateProblemForm::problemTypeInt, TomSelect(
                                options = ProblemType.entries.map {
                                    it.ordinal.toString() to it.name
                                },
                                label = "题目类型"
                            )
                        )
                        add(
                            CreateProblemForm::problemSupportLanguages, TomSelect(
                                options = SupportLanguages.entries.map {
                                    it.languageId.toString() to it.toString()
                                },
                                multiple = true,
                                label = "题目支持的语言类型"
                            )
                        )
                    }

                    panel.getChildren().forEach {
                        it.addCssClass("small")
                    }
                    sync(div { }) { testCases ->
                        button("提交").bind(AuthenticationModel.userToken) { token ->
                            if (token == null) {
                                disabled = true
                                Messager.toastInfo("您还没有登入，暂时无法创建问题")
                            }

                            onClickLaunch {
                                Messager.toastInfo("已经提交请求，请稍等")
                                Messager.toastInfo(
                                    ProblemModel.create(
                                        panel.getData(),
                                        testCases ?: return@onClickLaunch Messager.toastInfo("内部错误")
                                    ).toString()
                                )
                            }
                        }
                    }
                }
            }
            div(className = "col") {
                alert(AlertType.Light) {
                    h4 {
                        +"测试样例"
                    }
                    sync(div { }) { cases ->
                        console.log(cases)
                        if (cases != null) {
                            TestCasesShower.from(cases.testCases, withoutTitle = true) { case ->
                                add {
                                    +"删除"

                                    onClick {
                                        setObv(cases.copy(testCases = cases.testCases - case, passer = cases.passer))
                                    }
                                }
                            }.load(this)
                        }

                        val testCaseForm = formPanel<ProblemTestCaseForm> {
                            add(ProblemTestCaseForm::name, Text(label = "测试样例名称"), required = true)
                            add(ProblemTestCaseForm::input, Text(label = "输入"), required = true)
                            add(ProblemTestCaseForm::output, Text(label = "输出"), required = true)
                            add(ProblemTestCaseForm::visibilityStr, TomSelect(
                                label = "类型",
                                options = TestCaseType.entries.map {
                                    it.ordinal.toString() to it.chinese
                                }
                            ), required = true)

                            getChildren().forEach { it.addCssClass("small") }
                        }
                        button("增加一个样例") {
                            onClickLaunch {
                                setObv(
                                    testCases.copy(
                                        (cases?.testCases ?: listOf()) +
                                                testCaseForm.getData().let { form ->
                                                    ProblemTestCase(
                                                        "",
                                                        form.name,
                                                        form.input,
                                                        form.output,
                                                        form.visibilityStr.let {
                                                            TestCaseType.entries[it.toIntOrNull()
                                                                ?: return@onClickLaunch Messager.toastInfo("测试样例类型无效")]
                                                        })
                                                }
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }
    }
    div {
        marginBottom = 30.px
    }
}
