package cn.llonvne.entity.problem

import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable

/**
 * [Code] 的评论
 */
@Serializable
data class ShareCodeComment(
    val commentId: Int? = null,
    val committerAuthenticationUserId: Int,
    val shareCodeId: Int,

    val content: String,

    //--- 数据库信息区 ---//
    val version: Int? = null,
    val createdAt: LocalDateTime? = null,
    val updatedAt: LocalDateTime? = null
)