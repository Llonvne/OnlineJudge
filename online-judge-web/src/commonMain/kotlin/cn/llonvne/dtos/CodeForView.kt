package cn.llonvne.dtos

import cn.llonvne.entity.problem.JudgeResult
import cn.llonvne.entity.problem.Language
import cn.llonvne.entity.problem.SubmissionStatus
import kotlinx.serialization.Serializable

@Serializable
data class CodeForView(
    val rawCode: String,
    val language: Language,
    val problemName: String,
    val problemId: Int,
    val status: SubmissionStatus,
    val submissionId: Int,
    val judgeResult: JudgeResult,
)
