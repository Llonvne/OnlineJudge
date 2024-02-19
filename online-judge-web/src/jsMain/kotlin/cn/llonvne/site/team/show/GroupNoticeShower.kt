package cn.llonvne.site.team.show

import cn.llonvne.compoent.AlertType
import cn.llonvne.compoent.alert
import cn.llonvne.kvision.service.GroupIdNotFound
import cn.llonvne.kvision.service.IGroupService
import cn.llonvne.kvision.service.IGroupService.LoadGroupResp.GuestLoadGroup
import cn.llonvne.kvision.service.PermissionDenied
import io.kvision.core.Container
import io.kvision.html.h4
import io.kvision.panel.Root

interface GroupNoticeShower {
    fun load(root: Container)

    companion object {
        fun from(resp: IGroupService.LoadGroupResp): GroupNoticeShower {
            return when (resp) {
                is GroupIdNotFound -> emptyNoticeShower
                is GuestLoadGroup -> GuestGroupNoticeShower(resp)
                PermissionDenied -> emptyNoticeShower
            }
        }

        private val emptyNoticeShower = object : GroupNoticeShower {
            override fun load(root: Container) {
            }
        }
    }
}

private class GuestGroupNoticeShower(private val resp: GuestLoadGroup) : GroupNoticeShower {
    override fun load(root: Container) {
        root.alert(AlertType.Light) {
            h4 {
                +"公告"
            }
        }
    }
}