package cn.llonvne.compoent.team

import cn.llonvne.AppScope
import cn.llonvne.entity.group.GroupId
import cn.llonvne.entity.group.GroupVisibility
import cn.llonvne.kvision.service.GroupIdNotFound
import cn.llonvne.kvision.service.IGroupService.JoinGroupResp.Joined
import cn.llonvne.kvision.service.IGroupService.JoinGroupResp.Reject
import cn.llonvne.kvision.service.PermissionDenied
import cn.llonvne.message.Messager
import cn.llonvne.model.TeamModel
import cn.llonvne.security.AuthenticationToken
import io.kvision.routing.Routing
import kotlinx.coroutines.launch

class JoinGroupResolver(private val routing: Routing) {
    fun resolve(groupId: GroupId, groupName: String, token: AuthenticationToken) {
        AppScope.launch {
            when (TeamModel.join(token, groupId)) {
                is GroupIdNotFound -> Messager.toastError("小组不存在，可能是小组已经被删除")
                is Joined -> joined(groupName, groupId)
                PermissionDenied -> Messager.toastError("您还未登入，或者登入凭证已失效")
                is Reject -> Messager.toastInfo("你的加入请求被拒绝")
            }
        }
    }

    private fun joined(groupName: String, groupId: GroupId) {
        Messager.toastInfo("加入成功，$groupName 欢迎您")
        routing.navigate("/team/${groupId.path}")
    }
}