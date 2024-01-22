package cn.llonvne.site.share

import cn.llonvne.AppScope
import cn.llonvne.compoent.AlertType
import cn.llonvne.compoent.alert
import cn.llonvne.compoent.badge
import cn.llonvne.compoent.codeHighlighter
import cn.llonvne.entity.problem.share.decr
import cn.llonvne.entity.types.badge.BadgeColor
import cn.llonvne.kvision.service.ICodeService
import cn.llonvne.kvision.service.PermissionDenied
import cn.llonvne.message.Messager
import cn.llonvne.model.RoutingModule
import io.kvision.core.Container
import io.kvision.core.onClick
import io.kvision.html.Div
import io.kvision.html.h3
import io.kvision.html.h4
import io.kvision.html.p
import kotlinx.coroutines.launch

interface ShareCodeHighlighter<ID> {

    val idPlaceHolder: ID
    val title: Div
    fun load(root: Container, resp: ICodeService.GetCodeResp) {
        AppScope.launch {
            when (resp) {
                ICodeService.CodeNotFound -> onNotFound()
                is ICodeService.GetCodeResp.SuccessfulGetCode -> {
                    root.onSuccess(resp)
                }

                PermissionDenied -> onNotFound()
            }
        }
    }

    private fun onNotFound() {
        title.alert(AlertType.Info) {
            h4 {
                +"未找到 ${this@ShareCodeHighlighter.idPlaceHolder} 的代码分享"
            }
            p {
                +"可能是对方设置了权限，也可能是不存在该分享"
            }
        }
        Messager.toastError("未找到对应分享，可能是代码错误，或者是对方设置了查看权限")
    }

    private fun Container.onSuccess(
        resp: ICodeService.GetCodeResp.SuccessfulGetCode,
    ) {
        title.alert(AlertType.Light) {
            h3 {
                +"${resp.codeDto.shareUsername} 的代码分享"
            }

            badge(BadgeColor.Green) {
                +resp.codeDto.visibilityType.name

                onClick {
                    CodeVisibilityChanger(resp.codeDto.codeId, resp.codeDto).change()
                }
            }
            badge(BadgeColor.Blue) {
                +"语言 ${resp.codeDto.language.toString()}"
            }
            badge(BadgeColor.Red) {
                +resp.codeDto.commentType.decr()
            }

            if (resp.codeDto.hashLink != null) {
                badge(BadgeColor.Golden) {
                    +"分享链接"
                    onClick {
                        RoutingModule.routing.navigate("/share/${resp.codeDto.hashLink}")
                    }
                }
            }
        }
        codeHighlighter(code = resp.codeDto.rawCode)
    }

    companion object {
        fun <ID> highlighterJsImpl(shareId: ID, alert: Div): ShareCodeHighlighter<ID> = HighlighterJs(shareId, alert)
    }
}

private class HighlighterJs<ID>(
    override val idPlaceHolder: ID, override val title: Div,
) : ShareCodeHighlighter<ID>