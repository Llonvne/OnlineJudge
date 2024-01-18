package cn.llonvne.site

import cn.llonvne.AppScope
import cn.llonvne.compoent.addPassword
import cn.llonvne.compoent.addUsername
import cn.llonvne.compoent.navigateButton
import cn.llonvne.constants.Frontend
import cn.llonvne.message.Messager
import cn.llonvne.model.AuthenticationModel
import io.kvision.core.Container
import io.kvision.form.formPanel
import io.kvision.html.button
import io.kvision.html.h1
import io.kvision.routing.Routing
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

@Serializable
internal data class LoginPanel(
    val username: String,
    val password: String
)

internal fun Container.loginPanel(routing: Routing) {
    h1 {
        +"Login"
    }

    navigateButton(routing, Frontend.Index)

    val loginPanel = formPanel<LoginPanel> {
        addUsername(LoginPanel::username)
        addPassword(LoginPanel::password)
    }

    button("登入") {
        onClick {
            AppScope.launch {
                if (loginPanel.validate()) {
                    val value = loginPanel.getData()
                    val result = AuthenticationModel.login(value.username, value.password)
                    Messager.send(result.message)
                    if (result.isOk()) {
                        routing.navigate("/")
                    }
                }
            }
        }
    }
}