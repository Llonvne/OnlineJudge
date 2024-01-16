package cn.llonvne.panel

import cn.llonvne.AppScope
import cn.llonvne.model.AuthenticationModel
import io.kvision.core.Container
import io.kvision.form.formPanel
import io.kvision.form.text.Password
import io.kvision.form.text.Text
import io.kvision.html.button
import io.kvision.routing.Routing
import kotlinx.browser.window
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

@Serializable
internal data class LoginPanel(
    val username: String,
    val password: String
)

internal fun Container.loginPanel(routing: Routing) {
    val loginPanel = formPanel<LoginPanel> {
        add(
            LoginPanel::username, Text(label = "username") {
                placeholder = "输入你的用户名"
            },
            required = true,
            requiredMessage = "必须输入你的用户名",
            validator = { text: Text ->
                val username = text.getValue() ?: return@add false
                username.all {
                    it.isLetterOrDigit() or (it == '_')
                } and (username.length in 3..40)
            },
            validatorMessage = { _ ->
                "用户名必须由数字字母下划线组成，长度在 3-40 之间"
            }
        )
        add(
            LoginPanel::password,
            Password(label = "password"),
            required = true,
            requiredMessage = "密码不得为空"
        )
    }

    button("登入") {
        onClick {
            AppScope.launch {
                if (loginPanel.validate()) {
                    val value = loginPanel.getData()
                    val result = AuthenticationModel.login(value.username, value.password)
                    if (result) {
                        window.alert("登入成功，欢迎${value.username}")
                        routing.navigate("/")
                    } else {
                        window.alert("登入失败")
                    }
                }
            }
        }
    }
}