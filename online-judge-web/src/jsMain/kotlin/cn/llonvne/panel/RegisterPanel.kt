package cn.llonvne.panel

import cn.llonvne.App
import cn.llonvne.AppScope
import cn.llonvne.model.AuthenticationModel
import io.kvision.core.Container
import io.kvision.form.formPanel
import io.kvision.form.text.Password
import io.kvision.form.text.Text
import io.kvision.html.button
import kotlinx.browser.window
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

@Serializable
internal data class RegisterForm(
    val username: String,
    val password: String
)

internal fun Container.registerPanel() {
    val registerPanel = formPanel<RegisterForm> {
        add(
            RegisterForm::username, Text(label = "username") {
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
            RegisterForm::password,
            Password(label = "password"),
            required = true,
            requiredMessage = "密码不得为空"
        )
    }

    button("注册") {
        onClick {
            AppScope.launch {
                if (registerPanel.validate()) {
                    val value = registerPanel.getData()
                    window.alert(AuthenticationModel.register(value.username, value.password).toString())
                }
            }
        }
    }
}