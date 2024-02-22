package cn.llonvne.site.problem

import cn.llonvne.compoent.AlertType
import cn.llonvne.compoent.alert
import cn.llonvne.compoent.navigateButton
import cn.llonvne.constants.Frontend
import cn.llonvne.entity.problem.context.ProblemTestCases
import cn.llonvne.entity.problem.context.ProblemType
import cn.llonvne.entity.problem.context.ProblemVisibility
import cn.llonvne.entity.problem.context.passer.ProblemPasser
import cn.llonvne.gojudge.api.SupportLanguages
import cn.llonvne.message.Messager
import cn.llonvne.model.AuthenticationModel
import cn.llonvne.model.ProblemModel
import io.kvision.core.Container
import io.kvision.core.onClickLaunch
import io.kvision.form.formPanel
import io.kvision.form.number.Numeric
import io.kvision.form.select.TomSelect
import io.kvision.form.text.RichText
import io.kvision.form.text.Text
import io.kvision.form.text.TextArea
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

fun Container.createProblem(routing: Routing) {

    alert(AlertType.Success) {
        h1 {
            +"创建题目"
        }

        p {
            +"创建属于您自己的题目以供训练"
        }
    }

    val panel = formPanel<CreateProblemForm> { }

    val testCases = ProblemTestCases(listOf(), ProblemPasser.PassAllCases)

    div(className = "row") {
        div(className = "col") {
            alert(AlertType.Light) {
                with(panel) {
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

                button("提交").bind(AuthenticationModel.userToken) { token ->
                    if (token == null) {
                        disabled = true
                        Messager.toastInfo("您还没有登入，暂时无法创建问题")
                    }

                    onClickLaunch {
                        Messager.toastInfo("已经提交请求，请稍等")
                        Messager.toastInfo(
                            ProblemModel.create(
                                panel.getData()
                            ).toString()
                        )
                    }
                }
            }
        }
        div(className = "col") {
            alert(AlertType.Info) {
                h4 {
                    +"测试样例"
                }

                
            }
        }
    }


}