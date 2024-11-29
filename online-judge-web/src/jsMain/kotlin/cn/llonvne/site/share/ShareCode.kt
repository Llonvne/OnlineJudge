package cn.llonvne.site.share

import cn.llonvne.compoent.NotFoundAble
import cn.llonvne.compoent.notFound
import cn.llonvne.compoent.observable.observableOf
import cn.llonvne.entity.problem.share.Code.CodeType.*
import cn.llonvne.kvision.service.ICodeService.GetCodeResp
import cn.llonvne.site.JudgeResultDisplay
import io.kvision.core.Container
import io.kvision.html.Div
import io.kvision.html.div
import kotlinx.coroutines.Deferred
import kotlinx.serialization.Serializable

interface ShareID {
    data class IntId(
        val id: Int,
    ) : ShareID

    data class HashId(
        val hash: String,
    ) : ShareID
}

fun Container.share(
    hash: String,
    codeLoader: CodeLoader<String>,
) {
    val load = codeLoader.load(hash)
    shareInternal(load, ShareID.HashId(hash))
}

fun Container.share(
    shareId: Int,
    codeLoader: CodeLoader<Int>,
) {
    val load = codeLoader.load(shareId)
    shareInternal(load, ShareID.IntId(shareId))
}

private fun Container.shareInternal(
    load: Deferred<GetCodeResp>,
    id: ShareID,
) {
    observableOf<GetCodeResp?>(null) {
        setUpdater { load.await() }
        sync { resp ->
            resp
                ?.onSuccess { (code) ->
                    val highlighter = ShareCodeHighlighter.loadHighlighter(code, id, div { })

                    div(className = "row") {
                        div(className = "col") {
                            add(
                                Div {
                                    highlighter.load(this, code)
                                },
                            )

                            add(
                                Div {
                                    when (code.codeType) {
                                        Share -> {}
                                        Playground -> JudgeResultDisplay.playground(code.codeId, this)
                                        Problem -> JudgeResultDisplay.problem(code.codeId, this)
                                    }
                                },
                            )
                        }
                        div(className = "col") {
                            val shareCodeComment = ShareCodeCommentComponent.from(code.commentType, code)
                            shareCodeComment.loadComments(this)
                        }
                    }
                }?.onFailure {
                    notFound(
                        object : NotFoundAble {
                            override val header: String
                                get() = "未找到对应的代码分享/提交/训练场数据"
                            override val notice: String
                                get() = "有可能是该ID/Hash不存在，也可有可能是对方设置了权限"
                            override val errorCode: String
                                get() = "ShareNotFound-$id"
                        },
                    )
                }
        }
    }
}

@Serializable
data class CommentForm(
    val content: String?,
    val type: String,
)
