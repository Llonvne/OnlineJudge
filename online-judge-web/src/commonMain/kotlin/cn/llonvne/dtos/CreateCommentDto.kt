package cn.llonvne.dtos

import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable

@Serializable
data class CreateCommentDto(
    val commentId: Int,
    val committerUsername: String,
    val shareCodeId: Int,
    val content: String,
    val createdAt: LocalDateTime
)