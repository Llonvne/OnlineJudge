package cn.llonvne.compoent.team

import cn.llonvne.entity.group.GroupId
import cn.llonvne.kvision.service.GroupIdNotFound
import cn.llonvne.kvision.service.IGroupService.BeUpOrDowngradedUserNotfound
import cn.llonvne.kvision.service.IGroupService.DowngradeToMemberResp.DowngradeToMemberOk
import cn.llonvne.kvision.service.IGroupService.LoadGroupResp.GroupMemberDto
import cn.llonvne.kvision.service.IGroupService.UserAlreadyHasThisRole
import cn.llonvne.kvision.service.PermissionDeniedWithMessage
import cn.llonvne.message.Messager
import cn.llonvne.model.TeamModel

class DowngradeToGroupMemberResolver(
    private val groupId: GroupId,
) {
    suspend fun resolve(user: GroupMemberDto) {
        when (val resp = TeamModel.downgradeToMember(groupId, user.userId)) {
            is BeUpOrDowngradedUserNotfound -> Messager.toastInfo("用户不存在")
            is DowngradeToMemberOk -> Messager.toastInfo("降级成功")
            is GroupIdNotFound -> Messager.toastInfo("团队不存在")
            is PermissionDeniedWithMessage -> Messager.toastInfo(resp.message)
            is UserAlreadyHasThisRole -> Messager.toastInfo("用户已经是成员")
        }
    }
}
