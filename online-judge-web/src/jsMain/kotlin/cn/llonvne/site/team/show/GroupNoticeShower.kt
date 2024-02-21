package cn.llonvne.site.team.show

import cn.llonvne.compoent.AlertType
import cn.llonvne.compoent.alert
import cn.llonvne.kvision.service.GroupIdNotFound
import cn.llonvne.kvision.service.IGroupService
import cn.llonvne.kvision.service.IGroupService.LoadGroupResp
import cn.llonvne.kvision.service.IGroupService.LoadGroupResp.*
import cn.llonvne.kvision.service.PermissionDenied
import io.kvision.core.Container
import io.kvision.html.h4
import io.kvision.panel.Root

fun interface GroupNoticeShower {
    fun load(root: Container)

    companion object {
        fun from(resp: LoadGroupResp): GroupNoticeShower {
            return when (resp) {
                is GroupIdNotFound -> emptyNoticeShower
                is GuestLoadGroup -> GuestGroupNoticeShower(resp)
                PermissionDenied -> emptyNoticeShower
                is ManagerLoadGroup -> ManagerGroupNoticeShower(resp)
                is MemberLoadGroup -> MemberGroupNoticeShower(resp)
                is OwnerLoadGroup -> OwnerGroupNoticeShower(resp)
            }
        }

        private val emptyNoticeShower = GroupNoticeShower { }
    }
}

private abstract class AbstractGroupNoticeShower(private val resp: LoadGroupSuccessResp) :
    GroupNoticeShower {
    override fun load(root: Container) {
        root.alert(AlertType.Light) {
            h4 {
                +"公告"
            }
        }
    }
}

private class GuestGroupNoticeShower(private val resp: GuestLoadGroup) : AbstractGroupNoticeShower(resp)

private class ManagerGroupNoticeShower(private val resp: ManagerLoadGroup) : AbstractGroupNoticeShower(resp)

private class MemberGroupNoticeShower(private val resp: MemberLoadGroup) : AbstractGroupNoticeShower(resp)

private class OwnerGroupNoticeShower(private val resp: OwnerLoadGroup) : AbstractGroupNoticeShower(resp)