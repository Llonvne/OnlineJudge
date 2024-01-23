package cn.llonvne.entity.problem

import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable

@Serializable
data class Submission(
    val submissionId: Int? = null,
    val problemId: Int?,
    val codeId: Int,
    // 用户 ID
    val authenticationUserId: Int,

    // 可见性
    val visibility: SubmissionVisibilityType = SubmissionVisibilityType.PUBLIC,

    val status: SubmissionStatus = SubmissionStatus.Received,

    // 以 Json 形式存在内部
    val judgeResult: String,

    //--- 数据库信息区 ---//
    val version: Int? = null,
    val createdAt: LocalDateTime? = null,
    val updatedAt: LocalDateTime? = null
)
