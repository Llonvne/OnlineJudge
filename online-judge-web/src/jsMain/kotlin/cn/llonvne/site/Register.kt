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
internal data class RegisterForm(
    val username: String,
    val password: String
)

internal fun Container.registerPanel(routing: Routing) {
    h1 {
        +"Register"
    }

    navigateButton(routing, Frontend.Index)

    val registerPanel = formPanel<RegisterForm> {
        addUsername(RegisterForm::username)
        addPassword(RegisterForm::password)
    }

    button("注册") {
        onClick {
            AppScope.launch {
                if (registerPanel.validate()) {
                    val value = registerPanel.getData()
                    val result = AuthenticationModel.register(value.username, value.password)
                    Messager.send(result.message)
                    if (result.isOk()){
                        routing.navigate("/")
                    }
                }
            }
        }
    }
}