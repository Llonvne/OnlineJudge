package cn.llonvne.kvision.service

import kotlinx.serialization.Serializable

@Serializable
data object PermissionDenied : IProblemService.CreateProblemResp,
    ICodeService.SaveCodeResp, ICodeService.CommitOnCodeResp, ICodeService.SetCodeVisibilityResp,
    ICodeService.GetCodeResp, ICodeService.SetCodeCommentTypeResp,
    ICodeService.SetCodeCommentVisibilityTypeResp, ISubmissionService.CreateSubmissionResp,
    ISubmissionService.GetOutputByCodeIdResp, ISubmissionService.GetLastNPlaygroundSubmissionResp,
    ITeamService.CreateTeamResp