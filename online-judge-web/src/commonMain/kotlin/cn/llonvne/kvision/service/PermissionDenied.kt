package cn.llonvne.kvision.service

import cn.llonvne.kvision.service.ICodeService.*
import cn.llonvne.kvision.service.IGroupService.CreateGroupResp
import cn.llonvne.kvision.service.IGroupService.LoadGroupResp
import cn.llonvne.kvision.service.IProblemService.CreateProblemResp
import cn.llonvne.kvision.service.ISubmissionService.*
import kotlinx.serialization.Serializable

@Serializable
data object PermissionDenied :
    CreateProblemResp,
    SaveCodeResp,
    CommitOnCodeResp,
    SetCodeVisibilityResp,
    GetCodeResp,
    SetCodeCommentTypeResp,
    SetCodeCommentVisibilityTypeResp,
    CreateSubmissionResp,
    PlaygroundOutput,
    GetLastNPlaygroundSubmissionResp,
    CreateGroupResp,
    LoadGroupResp,
    IGroupService.JoinGroupResp,
    IGroupService.QuitGroupResp,
    IGroupService.KickGroupResp,
    ProblemSubmissionResp,
    GetLastNProblemSubmissionResp,
    IAuthenticationService.MineResp,
    IContestService.AddProblemResp,
    IContestService.CreateContestResp,
    IContestService.LoadContestResp,
    IContestService.ContextSubmissionResp,
    GetParticipantContestResp,
    IMineService.DashboardResp

@Serializable
data class PermissionDeniedWithMessage(
    val message: String,
) : IGroupService.KickGroupResp,
    IGroupService.UpgradeGroupManagerResp,
    IGroupService.DowngradeToMemberResp,
    ProblemSubmissionResp
