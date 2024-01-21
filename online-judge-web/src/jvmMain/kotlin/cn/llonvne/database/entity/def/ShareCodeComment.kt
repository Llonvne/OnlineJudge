package cn.llonvne.database.entity.def

import cn.llonvne.entity.problem.ShareCodeComment
import kotlinx.datetime.LocalDateTime
import org.komapper.annotation.*

@KomapperEntityDef(ShareCodeComment::class)
private data class ShareCodeCommentDef(
    @KomapperId @KomapperAutoIncrement
    val commentId: Int? = null,
    val committerAuthenticationUserId: Int,
    val shareCodeId: Int,

    val content: String,
    val type: Nothing,

    //--- 数据库信息区 ---//
    @KomapperVersion
    val version: Int? = null,

    @KomapperCreatedAt
    val createdAt: LocalDateTime? = null,

    @KomapperUpdatedAt
    val updatedAt: LocalDateTime? = null
)