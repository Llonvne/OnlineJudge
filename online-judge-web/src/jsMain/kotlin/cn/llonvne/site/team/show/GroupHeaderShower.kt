package cn.llonvne.site.team.show

import cn.llonvne.compoent.AlertType
import cn.llonvne.compoent.alert
import cn.llonvne.compoent.badge
import cn.llonvne.entity.types.badge.BadgeColor
import cn.llonvne.kvision.service.GroupIdNotFound
import cn.llonvne.kvision.service.IGroupService
import cn.llonvne.kvision.service.IGroupService.*
import cn.llonvne.kvision.service.IGroupService.LoadGroupResp.GuestLoadGroup
import io.kvision.core.Container
import io.kvision.html.h4
import io.kvision.html.p

sealed interface GroupHeaderShower {

    fun load(root: Container)

    companion object {
        fun from(resp: LoadGroupResp): GroupHeaderShower {
            return when (resp) {
                is GroupIdNotFound -> GroupIdNotFoundShower(resp)
                is GuestLoadGroup -> GuestGroupHeaderShower(resp)
            }
        }
    }
}

private class GroupIdNotFoundShower(private val resp: GroupIdNotFound) : GroupHeaderShower {
    override fun load(root: Container) {
        root.alert(AlertType.Danger) {
            h4 {
                +"Id 为 ${resp.groupId} 未找到"
            }

            p {
                +"可能是该小组不存在或者对方设置了查看权限"
            }
        }
    }
}

private class GuestGroupHeaderShower(private val resp: GuestLoadGroup) : GroupHeaderShower {
    override fun load(root: Container) {
        root.alert(AlertType.Light) {
            h4 {
                +resp.groupName
            }

            p {
                +resp.description
            }

            badge(BadgeColor.Golden) {
                +"所有者:${resp.ownerName}"
            }

            badge(BadgeColor.Grey) {
                +"短名称:${resp.groupShortName}"
            }

            badge(BadgeColor.Red) {
                +resp.visibility.shortChinese
            }

            badge(BadgeColor.Blue) {
                +resp.type.shortChinese
            }
        }
    }
}