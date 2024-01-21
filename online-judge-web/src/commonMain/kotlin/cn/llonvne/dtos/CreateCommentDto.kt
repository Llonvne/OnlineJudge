package cn.llonvne.dtos

import cn.llonvne.entity.problem.ShareCodeComment
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable

@Serializable
data class CreateCommentDto(
    val commentId: Int,
    val committerUsername: String,
    val shareCodeId: Int,
    val content: String,
    val createdAt: LocalDateTime,
    val visibilityType: ShareCodeComment.Companion.ShareCodeCommentType
)

fun CreateCommentDto.getVisibilityDecr():String = when(visibilityType){
    ShareCodeComment.Companion.ShareCodeCommentType.Deleted -> "已被删除"
    ShareCodeComment.Companion.ShareCodeCommentType.Public -> "对所有人可见"
    ShareCodeComment.Companion.ShareCodeCommentType.Private -> "仅对你与代码所有者可见"
}