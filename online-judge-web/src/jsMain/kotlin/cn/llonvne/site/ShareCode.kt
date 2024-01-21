package cn.llonvne.site

import cn.llonvne.AppScope
import cn.llonvne.compoent.*
import cn.llonvne.dtos.CreateCommentDto
import cn.llonvne.dtos.getVisibilityDecr
import cn.llonvne.entity.problem.ShareCodeComment
import cn.llonvne.entity.types.badge.BadgeColor
import cn.llonvne.kvision.service.ICodeService
import cn.llonvne.ll
import cn.llonvne.message.Messager
import cn.llonvne.model.AuthenticationModel
import cn.llonvne.model.CodeModel
import io.kvision.core.Container
import io.kvision.core.TextAlign
import io.kvision.core.onClick
import io.kvision.core.style
import io.kvision.dropdown.dropDown
import io.kvision.form.formPanel
import io.kvision.form.select.TomSelect
import io.kvision.form.text.TextArea
import io.kvision.html.*
import io.kvision.modal.Alert
import io.kvision.modal.Confirm
import io.kvision.state.ObservableListWrapper
import io.kvision.state.ObservableValue
import io.kvision.state.bind
import io.kvision.utils.px
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
    obComments: ObservableListWrapper<CreateCommentDto>,
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
        commentModule(obComments, codeId)
    }
}

@Serializable
private data class CommentForm(val content: String?, val type: String)

private fun Container.commentModule(obComments: ObservableListWrapper<CreateCommentDto>, codeId: Int) {
    div(className = "col") {

        alert(AlertType.Info) {
            h4 {
                +"留下你的友善评论"
            }
            p {
                +"根据我们的反垃圾信息政策，在代码所有者回复你之前，你至多可以发送 5 条评论"
            }

            formPanel<CommentForm>(className = "row") {
                "row gy-2 gx-3 align-items-center p-1".split(" ").forEach {
                    addCssClass(it)
                }

                add(
                    CommentForm::type, TomSelect(
                        label = "代码可见性",
                        options = listOf(
                            "1" to "对所有人可见", "2" to "仅对你与代码所有者可见"
                        ),
                    ) {
                        addCssClass("col")

                        maxWidth = 300.px
                    },
                    required = true,
                    requiredMessage = "必须选择一个代码可见性"
                )



                add(CommentForm::content, TextArea {
                    width = 600.px
                })

                button("提交") {

                    onClick {

                        if (AuthenticationModel.userToken.value == null) {
                            Messager.toastError("请先登入后发表评论")
                            return@onClick
                        }

                        AppScope.launch {

                            val username = AuthenticationModel.userToken.value?.username ?: return@launch

                            if (obComments.filter {
                                    it.committerUsername == username
                                }.size >= 5
                            ) {
                                Messager.toastInfo("根据我们的反垃圾信息政策，在代码所有者回复你之前，你至多可以发送 5 条评论")
                                return@launch
                            }


                            val data = getData()
                            if (data.content == null) {
                                Messager.toastInfo("评论不可为空")
                                return@launch
                            }
                            val type = when (data.type) {
                                "1" -> ShareCodeComment.Companion.ShareCodeCommentType.Public
                                "2" -> ShareCodeComment.Companion.ShareCodeCommentType.Private
                                else -> return@launch
                            }
                            Messager.toastInfo(CodeModel.commit(codeId, data.content, type).toString())
                            getComments(codeId, obComments)
                        }
                    }
                }
            }
        }


        div().bind(obComments) { comments ->
            comments.groupBy { dto ->
                dto.committerUsername
            }.forEach { (username, comments) ->
                alert(AlertType.Dark) {
                    val commentIds = comments.map { it.commentId }

                    h5 {
                        +"$username 评论:"
                    }

                    comments.forEach { comment ->
                        span {

                            alert(AlertType.Light) {
                                div {
                                    +comment.content
                                }

                                div {
                                    badge(BadgeColor.Red) {
                                        +"删除"
                                        onClick {
                                            deletePanel(codeId, listOf(comment.commentId), obComments)
                                        }
                                    }

                                    badge(BadgeColor.Blue) {
                                        +comment.getVisibilityDecr()
                                    }

                                    addCssStyle(style {
                                        textAlign = TextAlign.RIGHT
                                    })
                                }
                            }
                        }
                    }

                    val lastUpdate = comments.map { it.createdAt }.maxOf { it }

                    badge(BadgeColor.Green) {
                        +"最后更新于 ${lastUpdate.ll()}"
                    }
                    badge(BadgeColor.Red) {
                        +"删除全部"

                        onClick {
                            deletePanel(codeId, commentIds, obComments)
                        }
                    }
                }
            }
        }
    }
}

private fun Container.deletePanel(
    codeId: Int,
    commentIds: List<Int>,
    obComments: ObservableListWrapper<CreateCommentDto>
) {
    Messager.toastInfo("尝试删除${commentIds}")
    Confirm.show(
        "你正在尝试删除评论",
        "此操作将删除您的所有评论，请确认您的选择",
        animation = true,
        align = Align.LEFT,
        yesTitle = "删除",
        noTitle = "取消",
        cancelVisible = false,
        noCallback = {
            Alert.show("Result", "You pressed NO button.")
        },
        yesCallback = {
            AppScope.launch {
                val result = CodeModel.deleteCommentByIds(commentIds)
                if (result.isNotEmpty()) {
                    Alert.show("删除结果", "删除成功")
                } else {
                    Alert.show("删除结果", "删除失败")
                }
                getComments(codeId, obComments)
            }
        }
    )
}