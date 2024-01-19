package cn.llonvne.dtos

import cn.llonvne.entity.problem.Language
import cn.llonvne.entity.problem.SubmissionStatus
import cn.llonvne.gojudge.api.task.Output
import kotlinx.serialization.Serializable

@Serializable
data class ViewCodeDto(
    val rawCode: String,
    val language: Language,
    val problemName: String,
    val problemId: Int,
    val status: SubmissionStatus,
    val submissionId: Int,
    val output: Output?
)
