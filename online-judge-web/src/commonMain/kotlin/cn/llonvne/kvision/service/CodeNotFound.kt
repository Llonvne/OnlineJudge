package cn.llonvne.kvision.service

import kotlinx.serialization.Serializable

@Serializable
data object CodeNotFound : ICodeService.GetCodeResp, ICodeService.GetCommitsOnCodeResp,
    ICodeService.SetCodeVisibilityResp, ICodeService.SetCodeCommentTypeResp, ISubmissionService.ViewCodeGetByIdResp,
    ISubmissionService.SubmissionGetByIdResp, ISubmissionService.GetOutputByCodeIdResp