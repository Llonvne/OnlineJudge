package cn.llonvne.compoent.team

import cn.llonvne.entity.group.GroupId
import cn.llonvne.kvision.service.GroupIdNotFound
import cn.llonvne.kvision.service.IGroupService.KickGroupResp.*
import cn.llonvne.kvision.service.PermissionDenied
import cn.llonvne.kvision.service.PermissionDeniedWithMessage
import cn.llonvne.message.Messager
import cn.llonvne.model.TeamModel

class KickMemberResolver(
    private val groupId: GroupId,
) {
    suspend fun resolve(kickedMemberId: Int) {
        when (val resp = TeamModel.kick(groupId, kickedMemberId)) {
            is GroupIdNotFound -> Messager.toastInfo("小组不存在，可能是小组已经被删除")
            is KickMemberGroupIdRoleFound -> Messager.toastInfo("尝试踢出的成员未拥有本组权限(可能已经退出或已经被踢出)")
            is KickMemberNotFound -> Messager.toastInfo("被踢出的成员不存在")
            Kicked -> Messager.toastInfo("踢出成功")
            PermissionDenied -> Messager.toastInfo("您还未登入，或者登入凭证已失效")
            is PermissionDeniedWithMessage -> Messager.toastInfo(resp.message)
        }
    }
}
