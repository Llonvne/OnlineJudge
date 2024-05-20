package cn.llonvne.dtos

import kotlinx.serialization.Serializable

@Serializable
data class PlaygroudSubmission(
    val languageId: String?,
    val code: String?,
    val visibilityTypeStr: String? = null
)