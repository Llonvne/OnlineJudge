package cn.llonvne.site.team.show

import cn.llonvne.compoent.AlertType
import cn.llonvne.compoent.alert
import cn.llonvne.compoent.badge
import cn.llonvne.entity.role.*
import cn.llonvne.entity.types.badge.BadgeColor
import cn.llonvne.kvision.service.GroupIdNotFound
import cn.llonvne.kvision.service.IGroupService
import cn.llonvne.kvision.service.IGroupService.LoadGroupResp
import cn.llonvne.kvision.service.IGroupService.LoadGroupResp.GuestLoadGroup
import io.kvision.core.Container
import io.kvision.html.*
import io.kvision.tabulator.ColumnDefinition
import io.kvision.tabulator.Layout
import io.kvision.tabulator.TabulatorOptions
import io.kvision.tabulator.tabulator

interface GroupMemberShower {
    fun show(target: Container)

    companion object {
        fun from(resp: LoadGroupResp): GroupMemberShower {
            val memberShower = when (resp) {
                is GroupIdNotFound -> emptyMemberShower
                is GuestLoadGroup -> GuestMemberShower(resp)
            }
            return memberShower
        }

        private val emptyMemberShower = object : GroupMemberShower {
            override fun show(target: Container) {}
        }
    }
}

private class GuestMemberShower(private val resp: GuestLoadGroup) : GroupMemberShower {
    override fun show(target: Container) {
        target.alert(AlertType.Light) {

            h4 {
                +"成员列表"
            }

            tabulator(
                resp.members,
                options = TabulatorOptions(
                    layout = Layout.FITDATASTRETCH, columns = listOf(
                        ColumnDefinition("用户名", formatterComponentFunction = { _, _, e ->
                            span {
                                +e.username
                            }
                        }),
                        ColumnDefinition("身份", formatterComponentFunction = { _, _, e ->
                            span {
                                when (e.role) {
                                    is DeleteTeam.DeleteTeamImpl -> {}
                                    is GroupManager.GroupMangerImpl -> badge(BadgeColor.Red) {
                                        +"所有者"
                                    }

                                    is InviteMember.InviteMemberImpl -> {}
                                    is KickMember.KickMemberImpl -> {}
                                    is TeamMember.TeamMemberImpl -> {
                                        badge(BadgeColor.Grey) {
                                            +"成员"
                                        }
                                    }

                                    is TeamSuperManager.TeamSuperManagerImpl -> badge(BadgeColor.Dark) {
                                        +"队伍超级管理员"
                                    }
                                }
                            }
                        })
                    )
                )
            )
        }
    }
}