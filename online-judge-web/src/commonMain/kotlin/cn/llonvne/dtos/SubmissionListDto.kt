package cn.llonvne.dtos

import cn.llonvne.entity.AuthenticationUser
import cn.llonvne.entity.problem.Language
import cn.llonvne.entity.problem.Problem
import cn.llonvne.entity.problem.Submission
import cn.llonvne.entity.problem.SubmissionStatus
import cn.llonvne.gojudge.api.task.Output
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable

@Serializable
data class SubmissionListDto(
    val language: Language,
    val user: AuthenticationUserDto,
    val problemId: Int,
    val problemName: String,
    val submissionId: Int,
    val status: SubmissionStatus,
    val runTime: String,
    val runMemory: String,
    val codeLength: Long,
    val submitTime: LocalDateTime
)
