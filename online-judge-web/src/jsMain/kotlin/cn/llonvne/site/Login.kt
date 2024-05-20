package cn.llonvne.site

import cn.llonvne.AppScope
import cn.llonvne.compoent.addPassword
import cn.llonvne.compoent.addUsername
import cn.llonvne.message.Messager
import cn.llonvne.model.AuthenticationModel
import io.kvision.form.formPanel
import io.kvision.html.button
import io.kvision.modal.Dialog
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

@Serializable
internal data class LoginPanel(
    val username: String,
    val password: String
)

internal fun loginPanel() {

    val dialog = Dialog(
        "登录",
    ) {

        val loginPanel = formPanel<LoginPanel> {
            addUsername(LoginPanel::username)
            addPassword(LoginPanel::password)
        }

        button("登录") {
            onClick {
                setResult(loginPanel)
            }
        }
    }


    AppScope.launch {
        val data = dialog.getResult()
        if (data == null) {
            Messager.toastError("登入失败")
            return@launch
        }

        if (data.validate()) {
            val value = data.getData()
            kotlin.runCatching {
                val result = AuthenticationModel.login(value.username, value.password)
                Messager.send(result.message)
            }.onFailure {
                Messager.toastError("请检查你的网络设置")
            }
        }
    }
}