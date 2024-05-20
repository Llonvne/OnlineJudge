package cn.llonvne.dtos

import cn.llonvne.entity.problem.ShareCodeComment.Companion.ShareCodeCommentType
import cn.llonvne.entity.problem.ShareCodeComment.Companion.ShareCodeCommentType.*
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable

@Serializable
data class CreateCommentReq(
    val commentId: Int,
    val committerUsername: String,
    val shareCodeId: Int,
    val content: String,
    val createdAt: LocalDateTime,
    val visibilityType: ShareCodeCommentType
)

/**
 * 获取评论的可见性中文描述
 */
fun CreateCommentReq.getVisibilityDecr(): String = when (visibilityType) {
    Deleted -> "已被删除"
    Public -> "对所有人可见"
    Private -> "仅对你与代码所有者可见"
}