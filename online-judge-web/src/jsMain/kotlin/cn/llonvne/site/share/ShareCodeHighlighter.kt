package cn.llonvne.site.share

import cn.llonvne.AppScope
import cn.llonvne.compoent.AlertType
import cn.llonvne.compoent.alert
import cn.llonvne.compoent.badge
import cn.llonvne.compoent.codeHighlighter
import cn.llonvne.dtos.CodeDto
import cn.llonvne.entity.problem.share.Code
import cn.llonvne.entity.problem.share.Code.CodeType.*
import cn.llonvne.entity.types.badge.BadgeColor
import cn.llonvne.kvision.service.CodeNotFound
import cn.llonvne.kvision.service.ICodeService
import cn.llonvne.kvision.service.PermissionDenied
import cn.llonvne.message.Messager
import cn.llonvne.model.RoutingModule
import cn.llonvne.site.share.visisbility.CodeVisibilityChanger
import cn.llonvne.site.share.visisbility.CommentVisibilityChanger
import io.kvision.core.Container
import io.kvision.core.onClick
import io.kvision.html.Div
import io.kvision.html.h3
import io.kvision.html.h4
import io.kvision.html.p
import kotlinx.coroutines.launch

interface ShareCodeHighlighter {

    val idPlaceHolder: ShareID
    val title: Div
    val shareName: String

    fun load(root: Container, resp: ICodeService.GetCodeResp) {
        AppScope.launch {
            when (resp) {
                CodeNotFound -> onNotFound()
                is ICodeService.GetCodeResp.SuccessfulGetCode -> {
                    root.onSuccess(resp)
                }

                PermissionDenied -> onNotFound()
            }
        }
    }

    fun Container.slotOnTitle() {}

    private fun onNotFound() {
        title.alert(AlertType.Info) {
            h4 {
                +"未找到 ${this@ShareCodeHighlighter.idPlaceHolder} 的${shareName}"
            }
            p {
                +"可能是对方设置了权限，也可能是不存在该${shareName}"
            }
        }
        Messager.toastError("未找到对应${shareName}，可能是ID错误，或者是对方设置了查看权限")
    }

    private fun Container.onSuccess(
        resp: ICodeService.GetCodeResp.SuccessfulGetCode,
    ) {
        title.alert(AlertType.Light) {
            h3 {
                +"${resp.codeDto.shareUsername} 的${shareName}"
            }

            slotOnTitle()

            badge(BadgeColor.Green) {
                +resp.codeDto.visibilityType.reprName

                onClick {
                    CodeVisibilityChanger(resp.codeDto).change()
                }
            }
            badge(BadgeColor.Blue) {
                +"语言 ${resp.codeDto.language.toString()}"
            }
            badge(BadgeColor.Red) {
                +resp.codeDto.commentType.decr

                onClick {
                    CommentVisibilityChanger(resp.codeDto).change()
                }
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
        private fun highlighterJsImpl(shareId: ShareID, alert: Div): ShareCodeHighlighter =
            HighlighterJs(shareId, alert)

        private fun playgroundHighlighterJsImpl(share: ShareID, alert: Div): ShareCodeHighlighter =
            PlaygroundHighlighterJs(share, alert)

        fun loadHighlighter(codeDto: CodeDto, shareId: ShareID, alert: Div): ShareCodeHighlighter =
            when (codeDto.codeType) {
                Share -> highlighterJsImpl(shareId = shareId, alert = alert)
                Playground -> playgroundHighlighterJsImpl(shareId, alert)
            }
    }
}

private class HighlighterJs(
    override val idPlaceHolder: ShareID, override val title: Div, override val shareName: String = "分享",
) : ShareCodeHighlighter

private class PlaygroundHighlighterJs(
    override val idPlaceHolder: ShareID, override val title: Div, override val shareName: String = "训练场"
) : ShareCodeHighlighter{
    override fun Container.slotOnTitle() {
        p {
            +"训练场提交数据默认为私有，如果要与他人分享，请点击下方 <私有> 更改可见性"
        }
    }
}