package cn.llonvne.site

import cn.llonvne.AppScope
import cn.llonvne.compoent.*
import cn.llonvne.dtos.CreateCommentDto
import cn.llonvne.entity.types.BadgeColor
import cn.llonvne.kvision.service.ICodeService
import cn.llonvne.ll
import cn.llonvne.message.Messager
import cn.llonvne.model.AuthenticationModel
import cn.llonvne.model.CodeModel
import io.kvision.core.Container
import io.kvision.form.form
import io.kvision.form.formPanel
import io.kvision.form.text.Text
import io.kvision.form.text.TextArea
import io.kvision.html.*
import io.kvision.state.ObservableListWrapper
import io.kvision.state.ObservableValue
import io.kvision.state.bind
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

private suspend fun Container.getComments(shareId: Int, comments: ObservableListWrapper<CreateCommentDto>) {
    when (val commentsResp = CodeModel.getCommentByCodeId(shareId)) {
        ICodeService.CodeNotFound -> Messager.toastError("尝试获取评论失败")
        is ICodeService.GetCommitsOnCodeResp.SuccessfulGetCommits -> {
            Messager.toastInfo(commentsResp.commits.toString())
            singleRenderAsync {
                comments.clear()
                comments.addAll(commentsResp.commits)
            }
        }
    }
}

fun Container.share(shareId: Int) {
    val code = ObservableValue<ICodeService.GetCodeResp?>(null)
    val comments = ObservableListWrapper<CreateCommentDto>()

    val alert = div { }
    AppScope.launch {
        code.value = CodeModel.getCode(shareId)
        getComments(shareId, comments)
    }

    div().bind(code) {
        if (it == null) {
            loading()
        } else {
            when (it) {
                ICodeService.CodeNotFound -> {
                    onNotFound(alert, shareId)
                }

                is ICodeService.GetCodeResp.SuccessfulGetCode -> {
                    onSuccess(alert, it, comments, shareId)
                }
            }
        }
    }
}

private fun onNotFound(alert: Div, shareId: Int) {
    alert.alert(AlertType.Info) {
        h4 {
            +"未找到 $shareId 的代码分享"
        }
        p {
            +"可能是对方设置了权限，也可能是不存在该分享"
        }
    }

    Messager.toastError("未找到对应分享，可能是代码错误，或者是对方设置了查看权限")
}

private fun Container.onSuccess(
    alert: Div,
    resp: ICodeService.GetCodeResp.SuccessfulGetCode,
    comments: ObservableListWrapper<CreateCommentDto>,
    codeId: Int
) {
    div(className = "row") {
        div(className = "col-6") {
            alert.alert(AlertType.Light) {
                h3 {
                    +"${resp.codeDto.shareUsername} 的代码分享"
                }

                badge(BadgeColor.Green) {
                    +resp.codeDto.visibilityType.name
                }
                badge(BadgeColor.Blue) {
                    +"语言 ${resp.codeDto.language.toString()}"
                }
            }
            codeHighlighter(code = resp.codeDto.rawCode)
        }
        commentModule(comments, codeId)
    }
}

@Serializable
private data class CommentForm(val content: String?)

private fun Container.commentModule(comments: ObservableListWrapper<CreateCommentDto>, codeId: Int) {
    div(className = "col") {
        +"留下你的友善评论"

        formPanel<CommentForm> {
            add(CommentForm::content, TextArea() {
            })
            button("提交") {
                if (AuthenticationModel.userToken.value == null) {
                    disabled = true
                }

                onClick {
                    AppScope.launch {
                        val data = getData()
                        if (data.content == null) {
                            Messager.toastInfo("评论不可为空")
                            return@launch
                        }
                        Messager.toastInfo(CodeModel.commit(codeId, data.content).toString())
                        getComments(codeId, comments)
                    }
                }
            }
        }


        div().bind(comments) { comments ->
            comments.forEach { comment ->
                alert(AlertType.Dark) {
                    h5 {
                        +"${comment.committerUsername}:"
                    }
                    div {
                        +comment.content
                    }

                    badge(BadgeColor.Green) {
                        +comment.createdAt.ll()
                    }
                }
            }
        }
    }
}