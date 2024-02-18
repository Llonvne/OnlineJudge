package cn.llonvne.entity.group

import cn.llonvne.kvision.service.IGroupService

fun interface GroupLoader {
    suspend fun load(groupId: GroupId): IGroupService.LoadGroupResp

    companion object
}