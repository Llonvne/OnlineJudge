package cn.llonvne.kvision.service

import cn.llonvne.kvision.service.IContestService
import kotlinx.serialization.Serializable

@Serializable
object ContestNotFound : IContestService.LoadContestResp, IContestService.ContextSubmissionResp