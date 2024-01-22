package cn.llonvne.site.share

import cn.llonvne.AppScope
import cn.llonvne.compoent.AlertType
import cn.llonvne.compoent.alert
import cn.llonvne.dtos.CreateCommentDto
import cn.llonvne.entity.problem.share.CodeCommentType
import cn.llonvne.kvision.service.ICodeService
import cn.llonvne.message.Messager
import cn.llonvne.model.CodeModel
import io.kvision.core.Container
import io.kvision.html.h4
import io.kvision.state.ObservableListWrapper
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

/**
 * 该接口负责管理评论
 */
interface ShareCodeCommentComponent<Comment> {

    val shareId: Int
    val comments: ObservableListWrapper<Comment>

    fun loadComments(root: Container)

    fun refreshComments(): Deferred<Boolean>

    fun getComments(): ObservableListWrapper<Comment>

    companion object {

        fun from(status: CodeCommentType, shareId: Int): ShareCodeCommentComponent<*> {
            return when (status) {
                CodeCommentType.Open -> public(shareId)
                CodeCommentType.Closed -> empty("评论区已被代码所有者关闭")
                CodeCommentType.ClosedByAdmin -> empty("评论区已被管理员关闭")
                CodeCommentType.Protected -> protected(shareId)
            }
        }

        private fun empty(title: String) = object : ShareCodeCommentComponent<Nothing> {
            override val shareId: Int = 0
            override val comments: ObservableListWrapper<Nothing> = ObservableListWrapper()
            override fun loadComments(root: Container) {
                root.alert(AlertType.Secondary) {
                    h4 {
                        +title
                    }
                }
            }

            override fun getComments(): ObservableListWrapper<Nothing> {
                return comments
            }

            override fun refreshComments(): Deferred<Boolean> {
                return AppScope.async { true }
            }
        }

        private fun public(shareId: Int): ShareCodeCommentComponent<CreateCommentDto> =
            PublicShareCommentComponent(shareId)

        private fun protected(shareId: Int): ShareCodeCommentComponent<*> {
            return ProtectedShareCommentComponent(shareId)
        }
    }
}

private class ProtectedShareCommentComponent(
    shareId: Int,
    comments: ObservableListWrapper<CreateCommentDto> = ObservableListWrapper()
) : PublicShareCommentComponent(shareId, comments) {
    override fun loadComments(root: Container) {
        AppScope.launch {
            if (refreshComments().await()) {
                CommentSubmitter.protected(shareId, this@ProtectedShareCommentComponent).load(root)
                CommentDisplay.public(shareId, this@ProtectedShareCommentComponent).load(root)
            }
        }
    }
}

private open class PublicShareCommentComponent(
    override val shareId: Int,
    override val comments: ObservableListWrapper<CreateCommentDto> = ObservableListWrapper()
) : ShareCodeCommentComponent<CreateCommentDto> {

    override fun refreshComments(): Deferred<Boolean> {
        return AppScope.async {
            when (val commentsResp = CodeModel.getCommentByCodeId(shareId)) {
                ICodeService.CodeNotFound -> {
                    Messager.toastError("尝试获取评论失败")
                    false
                }

                is ICodeService.GetCommitsOnCodeResp.SuccessfulGetCommits -> {
                    Messager.toastInfo(commentsResp.commits.toString())
                    comments.clear()
                    comments.addAll(commentsResp.commits)
                    true
                }
            }
        }
    }

    private fun loadUI(root: Container, submitter: CommentSubmitter, display: CommentDisplay) {
        submitter.load(root)
        display.load(root)
    }

    override fun loadComments(root: Container) {
        AppScope.launch {
            if (refreshComments().await()) {
                loadUI(
                    root,
                    CommentSubmitter.public(shareId, this@PublicShareCommentComponent),
                    CommentDisplay.public(shareId, this@PublicShareCommentComponent)
                )
            }
        }
    }

    override fun getComments(): ObservableListWrapper<CreateCommentDto> {
        return comments
    }
}