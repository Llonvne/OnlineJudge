package cn.llonvne.dtos

import kotlinx.serialization.Serializable

@Serializable
data class SubmissionSubmit(
    val languageId: String?,
    val code: String?,
    val visibilityTypeStr: String
)