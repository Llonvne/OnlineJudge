package cn.llonvne.kvision.service

import cn.llonvne.kvision.service.ICodeService.CommitOnCodeResp
import cn.llonvne.kvision.service.ISubmissionService.CreateSubmissionResp
import cn.llonvne.kvision.service.ISubmissionService.GetLastNPlaygroundSubmissionResp
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable

@Serializable
data class InternalError(val reason: String) : CommitOnCodeResp, CreateSubmissionResp,
    GetLastNPlaygroundSubmissionResp, IGroupService.CreateGroupResp, ISubmissionService.ProblemSubmissionResp

@Serializable
data class JudgeError(@Contextual val reason: String) : CreateSubmissionResp