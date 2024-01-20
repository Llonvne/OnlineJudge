package cn.llonvne.kvision.service

import kotlinx.serialization.Serializable

@Serializable
data class InternalError(val reason: String) : ICodeService.CommitOnCodeResp
