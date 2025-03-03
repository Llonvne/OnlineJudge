package cn.llonvne.site

import cn.llonvne.AppScope
import cn.llonvne.compoent.*
import cn.llonvne.constants.Frontend
import cn.llonvne.message.Messager
import cn.llonvne.model.AuthenticationModel
import io.kvision.core.Container
import io.kvision.form.formPanel
import io.kvision.html.button
import io.kvision.html.div
import io.kvision.html.h1
import io.kvision.html.h4
import io.kvision.routing.Routing
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

@Serializable
internal data class RegisterForm(
    val username: String,
    val password: String,
)

internal fun Container.registerPanel(routing: Routing) {
    val alert = div { }
    add(alert)

    h1 {
        +"Register"
    }

    navigateButton(routing, Frontend.Index)

    val registerPanel =
        formPanel<RegisterForm> {
            addUsername(RegisterForm::username)
            addPassword(RegisterForm::password)
        }

    button("注册") {
        onClick {
            AppScope.launch {
                if (registerPanel.validate()) {
                    val value = registerPanel.getData()
                    runCatching {
                        AuthenticationModel.register(value.username, value.password)
                    }.onFailure {
                        alert.alert(AlertType.Danger) {
                            h4 { +"请检查你的网络设置" }
                        }
                    }.onSuccess { result ->
                        Messager.send(result.message)
                        if (result.isOk()) {
                            routing.navigate("/")
                        }
                    }
                }
            }
        }
    }
}
