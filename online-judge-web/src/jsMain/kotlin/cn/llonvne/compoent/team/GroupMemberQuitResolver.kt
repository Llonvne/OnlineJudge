package cn.llonvne.compoent.team

import cn.llonvne.entity.group.GroupId
import cn.llonvne.kvision.service.GroupIdNotFound
import cn.llonvne.kvision.service.IGroupService
import cn.llonvne.kvision.service.PermissionDenied
import cn.llonvne.message.Messager
import cn.llonvne.model.TeamModel
import io.kvision.core.Container
import io.kvision.core.onClickLaunch
import io.kvision.html.button

class GroupMemberQuitResolver(
    private val groupId: GroupId
) {
    fun load(root: Container) {
        root.button("退出小组") {
            onClickLaunch {
                when (val resp = TeamModel.quit(groupId)) {
                    is GroupIdNotFound -> Messager.toastError("小组${resp.groupId}不存在")
                    PermissionDenied -> Messager.toastError("权限不足")
                    IGroupService.QuitOk -> Messager.toastInfo("退出成功")
                }
            }
        }
    }
}