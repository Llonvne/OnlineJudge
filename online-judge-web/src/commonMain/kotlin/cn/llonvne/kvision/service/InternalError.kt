package cn.llonvne.kvision.service

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable

@Serializable
data class InternalError(val reason: String) : ICodeService.CommitOnCodeResp, ISubmissionService.CreateSubmissionResp,
    ISubmissionService.GetLastNPlaygroundSubmissionResp

@Serializable
data class JudgeError(@Contextual val throwable: Throwable) : ISubmissionService.CreateSubmissionResp