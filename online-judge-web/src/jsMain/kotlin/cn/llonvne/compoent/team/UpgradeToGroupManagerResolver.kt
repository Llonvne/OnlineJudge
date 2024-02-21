package cn.llonvne.compoent.team

import cn.llonvne.entity.group.GroupId
import cn.llonvne.kvision.service.GroupIdNotFound
import cn.llonvne.kvision.service.IGroupService.LoadGroupResp.GroupMemberDto
import cn.llonvne.kvision.service.IGroupService.UpgradeGroupManagerResp.*
import cn.llonvne.kvision.service.PermissionDeniedWithMessage
import cn.llonvne.message.Messager
import cn.llonvne.model.TeamModel

class UpgradeToGroupManagerResolver(private val groupId: GroupId) {
    suspend fun resolve(user: GroupMemberDto) {
        when (val resp = TeamModel.upgradeGroupManger(groupId, user.userId)) {
            is BeManagerNotFound -> Messager.toastInfo("未找到该用户在该小组-${user.username}")
            is GroupIdNotFound -> Messager.toastInfo("未找到该小组-${groupId}")
            is PermissionDeniedWithMessage -> Messager.toastInfo(resp.message)
            is UpdateToIdNotMatchToGroupId -> Messager.toastInfo("更新的角色不匹配")
            UpgradeManagerOk -> Messager.toastInfo("升级成功")
            is UserAlreadyHasThisRole -> Messager.toastInfo("该用户已经是管理员了")
        }
    }
}