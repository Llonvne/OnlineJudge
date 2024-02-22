package cn.llonvne.kvision.service

import cn.llonvne.kvision.service.ISubmissionService.PlaygroundOutput
import kotlinx.serialization.Serializable

@Serializable
data object JudgeResultParseError : PlaygroundOutput, ISubmissionService.ViewCodeGetByIdResp,
    ISubmissionService.GetJudgeResultByCodeIdResp