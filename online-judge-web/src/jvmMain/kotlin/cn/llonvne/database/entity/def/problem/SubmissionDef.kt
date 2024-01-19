package cn.llonvne.database.entity.def.problem

import cn.llonvne.entity.problem.Submission
import org.komapper.annotation.*

@KomapperEntityDef(entity = Submission::class)
private class SubmissionDef(
    @KomapperId
    @KomapperAutoIncrement
    val submissionId: Nothing,
    val problemId: Nothing,
    val languageId: Nothing,
    // 用户 ID
    val authenticationUserId: Nothing,

    val rowCode: Nothing,

    // 可见性
    val visibility: Nothing,

    val status: Nothing,

    // 以 Json 形式存在内部
    val judgeResult: Nothing,

    //--- 数据库信息区 ---//
    @KomapperVersion
    val version: Nothing,
    @KomapperCreatedAt
    val createdAt: Nothing,
    @KomapperUpdatedAt
    val updatedAt: Nothing
)