package cn.llonvne.site.team.show.member

import cn.llonvne.compoent.AlertType
import cn.llonvne.compoent.alert
import cn.llonvne.compoent.badge
import cn.llonvne.compoent.defineColumn
import cn.llonvne.compoent.team.*
import cn.llonvne.entity.role.*
import cn.llonvne.entity.role.GroupManager.GroupMangerImpl
import cn.llonvne.entity.role.InviteMember.InviteMemberImpl
import cn.llonvne.entity.role.KickMember.KickMemberImpl
import cn.llonvne.entity.role.TeamMember.TeamMemberImpl
import cn.llonvne.entity.role.TeamSuperManager.TeamSuperManagerImpl
import cn.llonvne.entity.types.badge.BadgeColor
import cn.llonvne.kvision.service.GroupIdNotFound
import cn.llonvne.kvision.service.IGroupService.LoadGroupResp
import cn.llonvne.kvision.service.IGroupService.LoadGroupResp.*
import cn.llonvne.kvision.service.PermissionDenied
import cn.llonvne.message.Messager
import cn.llonvne.model.AuthenticationModel
import cn.llonvne.model.RoutingModule
import io.kvision.core.Container
import io.kvision.core.onClickLaunch
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
                PermissionDenied -> emptyMemberShower
                is ManagerLoadGroup -> ManagerMemberShower(resp)
                is MemberLoadGroup -> MemberMemberShower(resp)
                is OwnerLoadGroup -> OwnerMemberShower(resp)
            }
            return memberShower
        }

        private val emptyMemberShower = object : GroupMemberShower {
            override fun show(target: Container) {}
        }
    }
}

/**
 * 读取小组成功后[LoadGroupSuccessResp]用于显示的基类
 */
private abstract class AbstractMemberShower(private val resp: LoadGroupSuccessResp) : GroupMemberShower {

    override fun show(target: Container) {
        target.alert(AlertType.Light) {

            div {
                loadButtons()
            }

            h4 {
                +"成员列表"
            }

            tabulator(
                resp.members,
                options = TabulatorOptions(
                    layout = Layout.FITDATASTRETCH, columns = defaultColumns() + addColumnDefinition()
                )
            )

            slot()
        }
    }

    private fun Div.defaultColumns(): List<ColumnDefinition<GroupMemberDto>> =
        listOf(
            ColumnDefinition("用户名", formatterComponentFunction = { _, _, e ->
                displayUsername(e)
            }),
            ColumnDefinition("身份", formatterComponentFunction = { _, _, e ->
                displayRole(e)
            })
        )

    protected open fun Div.displayRole(e: GroupMemberDto) = span {
        when (e.role) {
            is DeleteTeam.DeleteTeamImpl -> {}
            is GroupMangerImpl -> badge(BadgeColor.Red) { +"管理员" }
            is GroupOwner.GroupOwnerImpl -> badge(BadgeColor.Red) { +"所有者" }
            is InviteMemberImpl -> {}
            is KickMemberImpl -> {}
            is TeamMemberImpl -> {
                badge(BadgeColor.Grey) { +"成员" }
            }

            is TeamSuperManagerImpl -> badge(BadgeColor.Dark) {
                +"队伍超级管理员"
            }
        }
    }

    protected open fun Div.displayUsername(e: GroupMemberDto) = span {
        +e.username
    }

    protected open fun Div.addColumnDefinition(): List<ColumnDefinition<GroupMemberDto>> = listOf()
    open fun Container.loadButtons() {}
    protected open fun Div.slot() {}
}

private class GuestMemberShower(private val resp: GuestLoadGroup) : AbstractMemberShower(resp) {
    override fun Container.loadButtons() {
        button("现在加入") {
            onClick {
                val token =
                    AuthenticationModel.userToken.value ?: return@onClick Messager.toastInfo("登入后才可以加入小组")
                JoinGroupResolver(RoutingModule.routing)
                    .resolve(resp.groupId, resp.groupName, token)
            }
        }
    }
}

private class ManagerMemberShower(private val resp: ManagerLoadGroup) : AbstractMemberShower(resp) {

    private val kickMemberResolver = KickMemberResolver(groupId = resp.groupId)

    override fun Div.addColumnDefinition(): List<ColumnDefinition<GroupMemberDto>> = listOf(
        defineColumn("操作") { user ->
            span {
                KickOp.from(resp).load(this, user, kickMemberResolver)
            }
        }
    )
}


private fun Span.upgradeToGroupMangerOp(
    user: GroupMemberDto,
    upgradeToGroupManagerResolver: UpgradeToGroupManagerResolver
) = onNotSelf(user) {
    onNotManager(user) {
        badge(BadgeColor.Red) {
            onClickLaunch {
                upgradeToGroupManagerResolver.resolve(user)
            }
            +"升级为管理员"
        }
    }
}


private fun Span.downgradeToGroupMemberOp(
    user: GroupMemberDto,
    downgradeToGroupMemberResolver: DowngradeToGroupMemberResolver
) = onNotSelf(user) {
    onManager(user) {
        badge(BadgeColor.Red) {
            onClickLaunch {
                downgradeToGroupMemberResolver.resolve(user)
            }
            +"降级为成员"
        }
    }
}


private class OwnerMemberShower(private val resp: OwnerLoadGroup) : AbstractMemberShower(resp) {
    override fun Div.addColumnDefinition(): List<ColumnDefinition<GroupMemberDto>> = listOf(
        defineColumn("操作") { user ->
            span {
                KickOp.from(resp).load(this, user, KickMemberResolver(groupId = resp.groupId))
                upgradeToGroupMangerOp(user, UpgradeToGroupManagerResolver(groupId = resp.groupId))
                downgradeToGroupMemberOp(user, DowngradeToGroupMemberResolver(resp.groupId))
            }
        }
    )
}

private class MemberMemberShower(private val resp: MemberLoadGroup) : AbstractMemberShower(resp) {
    override fun Container.loadButtons() {
        GroupMemberQuitResolver(resp.groupId).load(this)
    }
}