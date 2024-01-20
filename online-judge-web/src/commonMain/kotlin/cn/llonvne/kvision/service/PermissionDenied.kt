package cn.llonvne.kvision.service

import kotlinx.serialization.Serializable

@Serializable
data object PermissionDenied : IProblemService.CreateProblemResp,
    ICodeService.SaveCodeResp, ICodeService.CommitOnCodeResp