package cn.llonvne.kvision.service

import cn.llonvne.kvision.service.ICodeService.*
import cn.llonvne.kvision.service.IGroupService.CreateGroupResp
import cn.llonvne.kvision.service.IGroupService.LoadGroupResp
import cn.llonvne.kvision.service.IProblemService.CreateProblemResp
import cn.llonvne.kvision.service.ISubmissionService.*
import kotlinx.serialization.Serializable

@Serializable
data object PermissionDenied : CreateProblemResp,
    SaveCodeResp, CommitOnCodeResp, SetCodeVisibilityResp,
    GetCodeResp, SetCodeCommentTypeResp,
    SetCodeCommentVisibilityTypeResp, CreateSubmissionResp,
    GetOutputByCodeIdResp, GetLastNPlaygroundSubmissionResp,
    CreateGroupResp, LoadGroupResp, IGroupService.JoinGroupResp, IGroupService.QuitGroupResp,
    IGroupService.KickGroupResp

@Serializable
data class PermissionDeniedWithMessage(val message: String) : IGroupService.KickGroupResp,
    IGroupService.UpgradeGroupManagerResp