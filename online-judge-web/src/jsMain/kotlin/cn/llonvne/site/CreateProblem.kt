package cn.llonvne.site

import cn.llonvne.AppScope
import cn.llonvne.compoent.navigateButton
import cn.llonvne.constants.Frontend
import cn.llonvne.message.Messager
import cn.llonvne.model.AuthenticationModel
import cn.llonvne.model.ProblemModel
import io.kvision.core.Container
import io.kvision.form.formPanel
import io.kvision.form.number.Numeric
import io.kvision.form.text.Text
import io.kvision.html.button
import io.kvision.html.h1
import io.kvision.routing.Routing
import io.kvision.state.bind
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

@Serializable
data class CreateProblemForm(
    val problemName: String,
    val problemDescription: String,
    val timeLimit: Long,
    val memoryLimit: Long,
    val authorId: Int
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
        add(CreateProblemForm::authorId, Numeric(min = 0))
    }

    button("提交").bind(AuthenticationModel.userToken) { token ->
        if (token == null) {
            disabled = true
            Messager.toastInfo("您还没有登入，暂时无法创建问题")
        }

        onClick {
            Messager.toastInfo("已经提交请求，请稍等")
            AppScope.launch {
                Messager.toastInfo(ProblemModel.create(createPanel.getData()).toString())
            }
        }
    }

}