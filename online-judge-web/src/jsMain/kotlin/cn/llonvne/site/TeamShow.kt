package cn.llonvne.site

import cn.llonvne.compoent.AlertType
import cn.llonvne.compoent.alert
import cn.llonvne.compoent.badge
import cn.llonvne.compoent.badgeGroup
import cn.llonvne.compoent.observable.observableOf
import cn.llonvne.entity.group.GroupId
import cn.llonvne.entity.group.GroupLoader
import cn.llonvne.entity.types.badge.BadgeColor
import cn.llonvne.kvision.service.GroupIdNotFound
import cn.llonvne.kvision.service.IGroupService
import cn.llonvne.kvision.service.IGroupService.LoadGroupResp
import cn.llonvne.kvision.service.IGroupService.LoadGroupResp.GuestLoadGroup
import cn.llonvne.model.TeamModel.of
import io.kvision.core.Container
import io.kvision.html.Div
import io.kvision.html.h4
import io.kvision.html.p
import io.kvision.routing.Routing

fun showGroup(root: Container, groupId: GroupId, routing: Routing) {
    val groupLoader = GroupLoader.of(groupId)
    GroupShower(groupLoader).show(root, routing)
}

private class GroupShower(private val groupLoader: GroupLoader) {

    private val target = Div()

    init {
        observableOf<LoadGroupResp>(null) {
            setUpdater {
                groupLoader.load()
            }

            sync(target) { resp ->
                if (resp == null) {
                    return@sync
                }

                when (resp) {
                    is GroupIdNotFound -> GroupIdNotFoundShower(resp).show(target)
                    is GuestLoadGroup -> GuestGroupShower(resp).show(target)
                }
            }
        }
    }

    fun show(root: Container, routing: Routing) {
        root.add(target)
    }
}

private class GroupIdNotFoundShower(private val resp: GroupIdNotFound) {
    fun show(target: Container) {
        target.alert(AlertType.Danger) {
            h4 {
                +"Id 为 ${resp.groupId} 未找到"
            }

            p {
                +"可能是该小组不存在或者对方设置了查看权限"
            }
        }
    }
}

private class GuestGroupShower(private val resp: GuestLoadGroup) {
    fun show(target: Container) {
        target.alert(AlertType.Light) {
            h4 {
                +resp.groupName
            }
            p {
                +"所有者:${resp.ownerName}"
            }
            p {
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