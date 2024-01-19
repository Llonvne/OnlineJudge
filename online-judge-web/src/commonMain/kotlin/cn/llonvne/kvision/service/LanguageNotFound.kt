package cn.llonvne.kvision.service

import kotlinx.serialization.Serializable

@Serializable
data object LanguageNotFound : ISubmissionService.SubmissionGetByIdResp, ISubmissionService.ViewCodeGetByIdResp,
    ICodeService.SaveCodeResp