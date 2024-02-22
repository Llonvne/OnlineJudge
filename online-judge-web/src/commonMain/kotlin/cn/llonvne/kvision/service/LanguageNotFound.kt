package cn.llonvne.kvision.service

import cn.llonvne.kvision.service.ICodeService.SaveCodeResp
import cn.llonvne.kvision.service.ISubmissionService.*
import kotlinx.serialization.Serializable

@Serializable
data object LanguageNotFound : SubmissionGetByIdResp, ViewCodeGetByIdResp,
    SaveCodeResp, CreateSubmissionResp,
    GetLastNPlaygroundSubmissionResp, GetOutputByCodeIdResp, ProblemSubmissionResp