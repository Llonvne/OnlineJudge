package cn.llonvne.kvision.service

import cn.llonvne.kvision.service.ICodeService.*
import cn.llonvne.kvision.service.ISubmissionService.*
import kotlinx.serialization.Serializable

@Serializable
data object CodeNotFound :
    GetCodeResp,
    GetCommitsOnCodeResp,
    SetCodeVisibilityResp,
    SetCodeCommentTypeResp,
    ViewCodeGetByIdResp,
    SubmissionGetByIdResp,
    PlaygroundOutput
