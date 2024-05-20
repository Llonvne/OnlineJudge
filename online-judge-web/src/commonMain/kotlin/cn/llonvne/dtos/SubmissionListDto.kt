package cn.llonvne.dtos

import cn.llonvne.entity.problem.Language
import cn.llonvne.entity.problem.SubmissionStatus
import cn.llonvne.entity.problem.context.passer.PasserResult
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable

@Serializable
data class SubmissionListDto(
    val language: Language,
    val user: Username,
    val problemId: Int,
    val problemName: String,
    val submissionId: Int,
    val status: SubmissionStatus,
    val codeLength: Long,
    val submitTime: LocalDateTime,
    val passerResult: PasserResult,
    val codeId: Int
)
