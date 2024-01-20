package cn.llonvne.dtos

import cn.llonvne.entity.problem.CodeVisibilityType
import cn.llonvne.entity.problem.Language
import kotlinx.serialization.Serializable

@Serializable
data class CodeDto(
    val rawCode: String,
    val language: Language?,
    val shareUserId: Int,
    val shareUsername: String,
    val visibilityType: CodeVisibilityType,
)