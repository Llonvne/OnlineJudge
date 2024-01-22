package cn.llonvne.site.share

import cn.llonvne.AppScope
import cn.llonvne.dtos.CodeDto
import cn.llonvne.entity.problem.share.CodeVisibilityType
import cn.llonvne.entity.problem.share.chinese
import cn.llonvne.entity.problem.share.decr
import cn.llonvne.message.Messager
import cn.llonvne.model.AuthenticationModel
import cn.llonvne.model.CodeModel
import io.kvision.html.*
import io.kvision.modal.Dialog
import io.kvision.toolbar.buttonGroup
import kotlinx.coroutines.launch

class CodeVisibilityChanger(val shareId: Int, val codeDto: CodeDto) {
    fun change() {
        val token = AuthenticationModel.userToken.value

        if (token == null) {
            Messager.toastError("你需要先验证你的身份才能更改代码可见性")
            return
        }
        if (token.authenticationUserId != codeDto.shareUserId) {
            Messager.toastError("只有代码所有者才能更改代码可见性")
            return
        }

        val dialog = Dialog("更改代码可见性") {

            p {
                CodeVisibilityType.entries.forEach { type ->
                    p {
                        +"${type.name}:${type.decr}"
                    }
                }
            }

            if (codeDto.visibilityType == CodeVisibilityType.Restrict) {
                p {
                    +"请注意你现在处于 ${codeDto.visibilityType.chinese} 模式中，一旦你更换到 公开/私密模式，将立刻使得目前的哈希链接失效，且不可恢复，如果你尝试再次切换到受限模式，将会生成一个新的哈希链接，并且使得旧的失效"
                }
            }

            buttonGroup {
                CodeVisibilityType.entries.forEach { type ->
                    button(type.chinese, style = ButtonStyle.OUTLINESECONDARY) {
                        onClick {
                            setResult(type)
                        }
                    }
                }
            }
        }

        AppScope.launch {
            val result = dialog.getResult()
            if (result == null) {
                Messager.toastInfo("你已经取消设置代码可见性")
            } else {
                when (val resp = CodeModel.setCodeVisibility(shareId, result)) {
                    cn.llonvne.kvision.service.ICodeService.CodeNotFound -> Messager.toastInfo("该分享代码不存在，或已被删除")
                    cn.llonvne.kvision.service.PermissionDenied -> Messager.toastInfo("你未登入，或者不是改代码所有者无法更改可见性")
                    cn.llonvne.kvision.service.ICodeService.SetCodeVisibilityResp.SuccessToPublicOrPrivate -> Messager.toastInfo(
                        "成功更改代码可见性"
                    )

                    is cn.llonvne.kvision.service.ICodeService.SetCodeVisibilityResp.SuccessToRestrict -> Messager.toastInfo(
                        "成功更改为受限类型,链接为${resp.link}"
                    )
                }
            }
        }
    }
}