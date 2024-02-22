package cn.llonvne.site.team.show.member

import cn.llonvne.entity.role.GroupManager
import cn.llonvne.entity.role.GroupOwner
import cn.llonvne.kvision.service.IGroupService.LoadGroupResp.GroupMemberDto
import cn.llonvne.model.AuthenticationModel

fun onNotSelf(user: GroupMemberDto, block: () -> Unit) {
    if (user.userId != AuthenticationModel.userToken.value?.id) {
        block()
    }
}

fun onNotManager(user: GroupMemberDto, block: () -> Unit) {
    if (user.role !is GroupManager) {
        block()
    }
}

fun onManager(user: GroupMemberDto, block: () -> Unit) {
    if (user.role is GroupManager) {
        block()
    }
}

fun onNotOwner(user: GroupMemberDto, block: () -> Unit) {
    if (user.role !is GroupOwner) {
        block()
    }
}