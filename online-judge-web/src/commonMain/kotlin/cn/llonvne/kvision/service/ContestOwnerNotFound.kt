package cn.llonvne.kvision.service

import cn.llonvne.kvision.service.IContestService
import kotlinx.serialization.Serializable

@Serializable
data object ContestOwnerNotFound : IContestService.LoadContestResp {
}