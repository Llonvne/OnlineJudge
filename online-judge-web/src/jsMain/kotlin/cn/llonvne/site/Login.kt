package cn.llonvne.site

import cn.llonvne.AppScope
import cn.llonvne.compoent.AlertType
import cn.llonvne.compoent.addPassword
import cn.llonvne.compoent.addUsername
import cn.llonvne.compoent.alert
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
internal data class LoginPanel(
    val username: String,
    val password: String
)

internal fun Container.loginPanel(routing: Routing) {
    val alert = div { }
    add(alert)

    h1 {
        +"Login"
    }

    val loginPanel = formPanel<LoginPanel> {
        addUsername(LoginPanel::username)
        addPassword(LoginPanel::password)
    }

    button("登入") {
        onClick {
            AppScope.launch {
                if (loginPanel.validate()) {
                    val value = loginPanel.getData()

                    kotlin.runCatching {
                        val result = AuthenticationModel.login(value.username, value.password)
                        Messager.send(result.message)
                        if (result.isOk()) {
                            routing.navigate("/")
                        }
                    }.onFailure {
                        alert.alert(AlertType.Danger) {
                            h4 { +"请检查你的网络设置" }
                        }
                    }
                }
            }
        }
    }
}