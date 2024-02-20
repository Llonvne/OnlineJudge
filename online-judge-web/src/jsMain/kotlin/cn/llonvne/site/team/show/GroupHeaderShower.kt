package cn.llonvne.site.team.show

import cn.llonvne.compoent.AlertType
import cn.llonvne.compoent.alert
import cn.llonvne.compoent.badge
import cn.llonvne.entity.types.badge.BadgeColor
import cn.llonvne.kvision.service.GroupIdNotFound
import cn.llonvne.kvision.service.IGroupService.*
import cn.llonvne.kvision.service.IGroupService.LoadGroupResp.GuestLoadGroup
import cn.llonvne.kvision.service.PermissionDenied
import cn.llonvne.ll
import cn.llonvne.message.Messager
import io.kvision.core.Container
import io.kvision.core.onClick
import io.kvision.html.h4
import io.kvision.html.p

sealed interface GroupHeaderShower {

    fun load(root: Container)

    companion object {
        fun from(resp: LoadGroupResp): GroupHeaderShower {
            return when (resp) {
                is GroupIdNotFound -> GroupIdNotFoundShower
                is GuestLoadGroup -> GuestGroupHeaderShower(resp)
                PermissionDenied -> GroupIdNotFoundShower
                is LoadGroupResp.ManagerLoadGroup -> ManagerGroupHeaderShower(resp)
                is LoadGroupResp.MemberLoadGroup -> MemberGroupHeaderShower(resp)
            }
        }
    }
}

private data object GroupIdNotFoundShower : GroupHeaderShower {
    override fun load(root: Container) {
        root.alert(AlertType.Danger) {
            h4 {
                +"对应小组未找到"
            }

            p {
                +"可能是该小组不存在或者对方设置了查看权限"
            }
        }
    }
}

private abstract class AbstractGroupHeaderShower(private val resp: LoadGroupResp.LoadGroupSuccessResp) :
    GroupHeaderShower {
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

                onClick {
                    Messager.toastInfo(resp.visibility.chinese)
                }
            }

            badge(BadgeColor.Blue) {
                +resp.type.shortChinese

                onClick {
                    Messager.toastInfo(resp.type.chinese)
                }
            }

            badge(BadgeColor.White) {
                +"创建于:${resp.createAt.ll()}"
            }

            slot()
        }
    }

    open fun Container.slot() {}
}

private open class GuestGroupHeaderShower(private val resp: GuestLoadGroup) : AbstractGroupHeaderShower(resp)

private class ManagerGroupHeaderShower(val resp: LoadGroupResp.ManagerLoadGroup) : AbstractGroupHeaderShower(resp)

private class MemberGroupHeaderShower(val resp: LoadGroupResp.MemberLoadGroup) : AbstractGroupHeaderShower(resp)
