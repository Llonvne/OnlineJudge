package cn.llonvne.site.share

import cn.llonvne.AppScope
import cn.llonvne.compoent.NotFoundAble
import cn.llonvne.compoent.notFound
import cn.llonvne.entity.problem.share.Code.CodeType.Playground
import cn.llonvne.entity.problem.share.Code.CodeType.Share
import cn.llonvne.kvision.service.ICodeService
import cn.llonvne.kvision.service.ICodeService.GetCodeResp
import cn.llonvne.message.Messager
import cn.llonvne.site.JudgeResultDisplay
import io.kvision.core.Container
import io.kvision.html.Div
import io.kvision.html.div
import io.kvision.state.ObservableValue
import io.kvision.state.bind
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

interface ShareID {
    data class IntId(val id: Int) : ShareID

    data class HashId(val hash: String) : ShareID
}


fun Container.share(
    hash: String,
    codeLoader: CodeLoader<String>,
    alert: Div
) {
    val load = codeLoader.load(hash)
    shareInternal(load, ShareID.HashId(hash), alert)
}

fun Container.share(
    shareId: Int,
    codeLoader: CodeLoader<Int>,
    alert: Div
) {
    val load = codeLoader.load(shareId)
    shareInternal(load, ShareID.IntId(shareId), alert)
}

private fun Container.shareInternal(load: Deferred<GetCodeResp>, id: ShareID, alert: Div) {

    val getCdeRespValue = ObservableValue<GetCodeResp?>(null)

    AppScope.launch {
        val resp = load.await()
        getCdeRespValue.value = resp
    }

    div { }.bind(getCdeRespValue) { resp ->
        resp?.onSuccess { (code) ->
            val highlighter = ShareCodeHighlighter.loadHighlighter(code, shareId = id, alert = alert)

            div(className = "row") {
                div(className = "col-6") {
                    highlighter.load(this, resp)

                    when (code.codeType) {
                        Share -> {}
                        Playground -> {
                            JudgeResultDisplay.playground(code.codeId, this)
                        }
                    }
                }
                div(className = "col") {
                    val shareCodeComment = ShareCodeCommentComponent.from(code.commentType, code)
                    shareCodeComment.loadComments(this@div)
                }
            }
        }?.onFailure {
            notFound(object : NotFoundAble {
                override val header: String
                    get() = "未找到对应的代码分享/提交/训练场数据"
                override val notice: String
                    get() = "有可能是该ID/Hash不存在，也可有可能是对方设置了权限"
                override val errorCode: String
                    get() = "ShareNotFound-${id}"
            })
        }
    }
}

@Serializable
data class CommentForm(val content: String?, val type: String)

