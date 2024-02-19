package cn.llonvne.kvision.service

import cn.llonvne.entity.group.GroupId
import kotlinx.serialization.Serializable

@Serializable
data object GroupShortNameUnavailable : IGroupService.CreateGroupResp

@Serializable
data class GroupIdNotFound(val groupId: GroupId) : IGroupService.LoadGroupResp