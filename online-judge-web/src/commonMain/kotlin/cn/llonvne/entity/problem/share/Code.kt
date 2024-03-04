package cn.llonvne.entity.problem.share

import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable


@Serializable
data class Code(
    val codeId: Int? = null,

    val authenticationUserId: Int,

    val code: String,
    val languageId: Int?,

    val codeType: CodeType,

    val visibilityType: CodeVisibilityType = CodeVisibilityType.Public,
    val commentType: CodeCommentType = CodeCommentType.Open,
    val hashLink: String? = null,

    //--- 数据库信息区 ---//
    val version: Int? = null,
    val createdAt: LocalDateTime? = null,
    val updatedAt: LocalDateTime? = null
) {
    @Serializable
    enum class CodeType {
        Share, Playground, Problem
    }
}


