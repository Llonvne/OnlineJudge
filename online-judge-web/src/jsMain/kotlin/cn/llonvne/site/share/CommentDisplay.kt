package cn.llonvne.site.share

import cn.llonvne.AppScope
import cn.llonvne.compoent.AlertType
import cn.llonvne.compoent.alert
import cn.llonvne.compoent.badge
import cn.llonvne.dtos.CreateCommentDto
import cn.llonvne.dtos.getVisibilityDecr
import cn.llonvne.entity.types.badge.BadgeColor
import cn.llonvne.ll
import cn.llonvne.model.CodeModel
import io.kvision.core.Container
import io.kvision.core.TextAlign
import io.kvision.core.onClick
import io.kvision.core.style
import io.kvision.html.Align
import io.kvision.html.div
import io.kvision.html.h5
import io.kvision.html.span
import io.kvision.modal.Alert
import io.kvision.modal.Confirm
import io.kvision.state.bind
import kotlinx.coroutines.launch

interface CommentDisplay {

    fun load(root: Container)

    companion object {
        fun empty() = object : CommentDisplay {
            override fun load(root: Container) {}
        }

        fun public(
            shareId: Int,
            shareCodeCommentComponent: ShareCodeCommentComponent<CreateCommentDto>
        ): CommentDisplay = PublicShareCodeCommentDisplay(shareId, shareCodeCommentComponent)
    }
}

private class PublicShareCodeCommentDisplay(
    val shareId: Int,
    val shareCodeCommentComponent: ShareCodeCommentComponent<CreateCommentDto>
) : CommentDisplay {
    override fun load(root: Container) {
        root.div().bind(
            shareCodeCommentComponent.getComments()
        ) { comments ->
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
                                            deletePanel(
                                                listOf(comment.commentId),
                                            )
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
                            deletePanel(commentIds)
                        }
                    }
                }
            }
        }
    }

    fun deletePanel(
        commentIds: List<Int>,
    ) {
        Confirm.show(
            "你正在尝试删除评论",
            "此操作将删除您的所有评论，请确认您的选择",
            animation = true,
            align = Align.LEFT,
            yesTitle = "删除",
            noTitle = "取消",
            cancelVisible = false,
            noCallback = {
                Alert.show("删除结果", "你取消了删除")
            },
            yesCallback = {
                AppScope.launch {
                    val result = CodeModel.deleteCommentByIds(commentIds)
                    if (result.isNotEmpty()) {
                        Alert.show("删除结果", "删除成功")
                    } else {
                        Alert.show("删除结果", "删除失败")
                    }
                    shareCodeCommentComponent.refreshComments()
                }
            }
        )
    }
}