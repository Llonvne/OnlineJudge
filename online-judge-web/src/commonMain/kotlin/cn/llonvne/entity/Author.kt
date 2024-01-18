package cn.llonvne.entity

import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable
import kotlin.Int

/**
 *  作者信息
 */
@Serializable
data class Author(
    val authorId: Int? = null,
    val authorName: String,
    val introduction: String,

    // 如果是注册用户，那么则绑定 AuthenticationUser 的ID
    val authenticationUserId: Int? = null,

    //--- 数据库信息区 ---//
    val version: Int? = null,
    val createdAt: LocalDateTime? = null,
    val updatedAt: LocalDateTime? = null
)