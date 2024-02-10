package cn.llonvne.site.share

import cn.llonvne.AppScope
import cn.llonvne.compoent.AlertType
import cn.llonvne.compoent.alert
import cn.llonvne.dtos.CodeDto
import cn.llonvne.dtos.CreateCommentDto
import cn.llonvne.entity.problem.ShareCodeComment
import cn.llonvne.entity.problem.ShareCodeComment.Companion.ShareCodeCommentType.Private
import cn.llonvne.entity.problem.ShareCodeComment.Companion.ShareCodeCommentType.Public
import cn.llonvne.message.Messager
import cn.llonvne.model.AuthenticationModel
import cn.llonvne.model.CodeModel
import io.kvision.core.Container
import io.kvision.form.formPanel
import io.kvision.form.select.TomSelect
import io.kvision.form.text.TextArea
import io.kvision.html.button
import io.kvision.html.h4
import io.kvision.html.p
import io.kvision.utils.px
import kotlinx.coroutines.launch

interface CommentSubmitter {
    fun load(root: Container)

    companion object {
        fun public(shareId: Int, shareCommentComponent: ShareCodeCommentComponent<CreateCommentDto>): CommentSubmitter =
            PublicCommentSubmitter(shareId, shareCommentComponent)

        fun protected(
            shareId: Int, code: CodeDto, shareCommentComponent: ShareCodeCommentComponent<CreateCommentDto>
        ): CommentSubmitter = ProtectedCommentSubmitter(shareId, shareCommentComponent, code)

        fun closed(): CommentSubmitter = object : CommentSubmitter {
            override fun load(root: Container) {
                root.alert(AlertType.Dark) {
                    +"评论区已经被冻结"
                }
            }
        }
    }
}

private class ProtectedCommentSubmitter(
    shareId: Int, shareCommentComponent: ShareCodeCommentComponent<CreateCommentDto>,
    private val code: CodeDto
) : PublicCommentSubmitter(shareId, shareCommentComponent) {
    override fun getCommentVisibilityOptions(): List<Pair<String, String>> {


        return super.getCommentVisibilityOptions().filter {
            if (AuthenticationModel.userToken.value?.id == null) {
                return@filter false
            }
            if (code.shareUserId != AuthenticationModel.userToken.value?.id) {
                it.first != PUBLIC_CODE
            } else {
                return@filter true
            }
        }
    }

    override fun notice(container: Container) {
        container.p {
            +"代码所有者已将该评论区设为保护状态，所有评论必须经过代码所有者审批才可以转换为公开状态"
        }
    }
}

private open class PublicCommentSubmitter(
    private val shareId: Int,
    private val shareCommentComponent: ShareCodeCommentComponent<CreateCommentDto>,
) : CommentSubmitter {

    companion object {
        const val PRIVATE_CODE = "1"
        const val PUBLIC_CODE = "2"
    }

    protected open fun getCommentVisibilityOptions(): List<Pair<String, String>> {
        return listOf(
            PUBLIC_CODE to "对所有人可见", PRIVATE_CODE to "仅对你与代码所有者可见"
        )
    }

    protected open fun notice(container: Container) {

    }

    protected open fun getCommentVisibilitySelect() = TomSelect(
        label = "代码可见性",
        options = getCommentVisibilityOptions(),
    ) {
        addCssClass("col")
        maxWidth = 300.px
    }

    override fun load(root: Container) {
        root.alert(AlertType.Info) {
            h4 {
                +"留下你的友善评论"
            }
            p {
                +"根据我们的反垃圾信息政策，在代码所有者回复你之前，你至多可以发送 5 条评论"
            }

            notice(this)

            formPanel<CommentForm>(className = "row") {
                "row gy-2 gx-3 align-items-center p-1".split(" ").forEach {
                    addCssClass(it)
                }

                add(
                    CommentForm::type,
                    getCommentVisibilitySelect(),
                    required = true,
                    requiredMessage = "必须选择一个代码可见性",
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

                            val username = AuthenticationModel.info()?.username ?: return@launch

                            if (shareCommentComponent.getComments().filter {
                                    it.committerUsername == username
                                }.size >= 5) {
                                Messager.toastInfo("根据我们的反垃圾信息政策，在代码所有者回复你之前，你至多可以发送 5 条评论")
                                return@launch
                            }

                            val data = getData()
                            if (data.content == null) {
                                return@launch Messager.toastInfo("评论不可为空")
                            }

                            val type = when (data.type) {
                                PUBLIC_CODE -> Public
                                PRIVATE_CODE -> Private
                                else -> return@launch Messager.toastInfo("无效的可见性选择")
                            }

                            Messager.toastInfo(CodeModel.commit(shareId, data.content, type).toString())
                            shareCommentComponent.refreshComments()
                        }
                    }
                }
            }
        }
    }
}