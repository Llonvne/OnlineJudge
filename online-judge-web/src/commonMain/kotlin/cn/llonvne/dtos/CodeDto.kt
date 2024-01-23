package cn.llonvne.dtos

import cn.llonvne.entity.problem.share.CodeVisibilityType
import cn.llonvne.entity.problem.Language
import cn.llonvne.entity.problem.share.Code
import cn.llonvne.entity.problem.share.CodeCommentType
import kotlinx.serialization.Serializable

@Serializable
data class CodeDto(
    val codeId: Int,
    val rawCode: String,
    val language: Language?,
    val shareUserId: Int,
    val shareUsername: String,
    val visibilityType: CodeVisibilityType,
    val commentType: CodeCommentType,
    val hashLink: String?,
    val codeType: Code.CodeType
)