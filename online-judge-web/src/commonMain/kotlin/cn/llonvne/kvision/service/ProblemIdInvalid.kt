package cn.llonvne.kvision.service

import kotlinx.serialization.Serializable

@Serializable
data object ProblemIdInvalid : IContestService.AddProblemResp, IContestService.CreateContestResp
