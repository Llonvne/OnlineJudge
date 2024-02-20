package cn.llonvne.kvision.service

import cn.llonvne.entity.group.GroupId
import cn.llonvne.kvision.service.IGroupService.*
import kotlinx.serialization.Serializable

@Serializable
data class GroupShortNameUnavailable(val shortName: String) : CreateGroupResp

@Serializable
data class GroupIdNotFound(val groupId: GroupId) : LoadGroupResp, JoinGroupResp, QuitGroupResp