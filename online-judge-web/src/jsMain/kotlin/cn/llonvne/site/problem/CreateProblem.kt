package cn.llonvne.site.problem

import cn.llonvne.compoent.navigateButton
import cn.llonvne.constants.Frontend
import cn.llonvne.entity.problem.context.ProblemType
import cn.llonvne.entity.problem.context.ProblemVisibility
import cn.llonvne.gojudge.api.SupportLanguages
import cn.llonvne.message.Messager
import cn.llonvne.model.AuthenticationModel
import cn.llonvne.model.ProblemModel
import io.kvision.core.Container
import io.kvision.core.onClickLaunch
import io.kvision.form.formPanel
import io.kvision.form.number.Numeric
import io.kvision.form.select.TomSelect
import io.kvision.form.text.Text
import io.kvision.form.text.TextArea
import io.kvision.html.button
import io.kvision.html.h1
import io.kvision.html.tr
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
    val problemSupportLanguages: String
)

fun Container.createProblem(routing: Routing) {
    h1 {
        +"Problem Create"
    }

    navigateButton(routing, Frontend.Index)

    val createPanel = formPanel<CreateProblemForm> {
        add(CreateProblemForm::problemName, Text(label = "题目名字") {
            addCssClass("small")
        })
        add(CreateProblemForm::problemDescription, Text(label = "题目描述") {
            placeholder = "一句话描述你的题目"
        })
        add(CreateProblemForm::timeLimit, Numeric(min = 0, max = 1_0000_0000, label = "时间限制"))
        add(CreateProblemForm::memoryLimit, Numeric(min = 0, max = 1_0000_0000, label = "内存限制"))
        add(CreateProblemForm::authorId, Numeric(min = 0, label = "作者ID"))
        add(
            CreateProblemForm::problemVisibilityInt, TomSelect(
                options = ProblemVisibility.entries.map {
                    it.ordinal.toString() to it.chinese
                }
            ) {
                placeholder = "题目可见性设置"
            }
        )
        add(
            CreateProblemForm::problemTypeInt, TomSelect(
                options = ProblemType.entries.map {
                    it.ordinal.toString() to it.name
                }
            ) {
                placeholder = "题目类型"
            })
        add(CreateProblemForm::overall, TextArea() {
            placeholder = "题目总体要做什么,解决什么问题"
        })
        add(CreateProblemForm::inputDescr, Text() {
            placeholder = "输入描述"
        })
        add(CreateProblemForm::outputDescr, Text() {
            placeholder = "输出描述"
        })
        add(CreateProblemForm::hint, Text() {
            placeholder = "题目提示"
        })
        add(
            CreateProblemForm::problemSupportLanguages, TomSelect(
                options = SupportLanguages.entries.map {
                    it.languageId.toString() to it.toString()
                },
                multiple = true
            )
        )
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
                    createPanel.getData()
                ).toString()
            )
        }

        marginBottom = 100.px
    }
}