package cn.llonvne.site.share.visisbility

import cn.llonvne.AppScope
import cn.llonvne.dtos.CodeDto
import cn.llonvne.entity.DescriptionGetter
import cn.llonvne.message.Messager
import cn.llonvne.model.AuthenticationModel
import io.kvision.core.Container
import io.kvision.html.ButtonStyle
import io.kvision.html.button
import io.kvision.html.p
import io.kvision.modal.Dialog
import io.kvision.toolbar.buttonGroup
import kotlinx.coroutines.launch

interface VisibilityChanger {

    val codeDto: CodeDto

    fun isCodeOwner(): Boolean {
        val token = AuthenticationModel.userToken.value

        if (token == null) {
            Messager.toastError("你需要先验证你的身份才能更改代码可见性")
            return false
        }
        if (token.id != codeDto.shareUserId) {
            Messager.toastError("只有代码所有者才能更改代码可见性")
            return false
        }

        return true
    }


    fun <T : DescriptionGetter> change(
        dialogTitle: String,
        choice: List<T>,
        onEach: Container.(T) -> Unit = {},
        slot: Container.() -> Unit = {},
        selectionCallback: suspend (T) -> Unit
    ) {
        val dialog = Dialog(dialogTitle) {
            choice.forEach {
                p {
                    +"${it.reprName}:${it.decr}"
                }

                onEach(it)
            }

            slot()

            buttonGroup {
                choice.forEach { c ->
                    button(c.reprName, style = ButtonStyle.OUTLINESECONDARY) {
                        onClick {
                            setResult(c)
                        }
                    }
                }
            }
        }

        AppScope.launch {
            val result = dialog.getResult()
            if (result != null) {
                selectionCallback(result)
            }
        }
    }
}