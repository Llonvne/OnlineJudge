package cn.llonvne.site.share

import cn.llonvne.AppScope
import cn.llonvne.compoent.loading
import cn.llonvne.entity.problem.share.CodeCommentType
import cn.llonvne.kvision.service.ICodeService
import io.kvision.core.Container
import io.kvision.html.Div
import io.kvision.html.div
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable


fun Container.share(
    hash: String,
    codeLoader: CodeLoader<String>,
    highlighter: ShareCodeHighlighter<String>
) {
    val load = codeLoader.load(hash)
    shareInternal(load, highlighter)
}

fun Container.share(
    shareId: Int,
    codeLoader: CodeLoader<Int>,
    highlighter: ShareCodeHighlighter<Int>,
) {

    val load = codeLoader.load(shareId)
    shareInternal(load, highlighter)
}

private fun Container.shareInternal(load: Deferred<ICodeService.GetCodeResp>, highlighter: ShareCodeHighlighter<*>) {
    AppScope.launch {
        val resp = load.await()

        div(className = "row") {
            div(className = "col-6") {
                highlighter.load(this, resp)
            }
            div(className = "col") {
                resp.onSuccess { (code) ->
                    val shareCodeComment = ShareCodeCommentComponent.from(code.commentType, code)
                    shareCodeComment.loadComments(this)
                }
            }
        }
    }
}

@Serializable
data class CommentForm(val content: String?, val type: String)

