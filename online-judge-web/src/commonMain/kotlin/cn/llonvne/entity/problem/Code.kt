package cn.llonvne.entity.problem

import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable

enum class CodeVisibilityType {
    Public, Private, Restrict
}

@Serializable
data class Code(
    val codeId: Int? = null,

    val authenticationUserId: Int,

    val code: String,
    val languageId: Int?,

    val visibilityType: CodeVisibilityType = CodeVisibilityType.Public,

    //--- 数据库信息区 ---//
    val version: Int? = null,
    val createdAt: LocalDateTime? = null,
    val updatedAt: LocalDateTime? = null
)