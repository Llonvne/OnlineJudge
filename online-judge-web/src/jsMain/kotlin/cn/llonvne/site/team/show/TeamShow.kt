package cn.llonvne.site.team.show

import cn.llonvne.compoent.observable.observableOf
import cn.llonvne.entity.group.GroupId
import cn.llonvne.entity.group.GroupLoader
import cn.llonvne.kvision.service.IGroupService.LoadGroupResp
import cn.llonvne.model.TeamModel.of
import cn.llonvne.site.team.show.member.GroupMemberShower
import io.kvision.core.Container
import io.kvision.html.div
import io.kvision.routing.Routing

fun showGroup(
    root: Container,
    groupId: GroupId,
    routing: Routing,
) {
    val groupLoader = GroupLoader.of(groupId)
    GroupShower(groupLoader).show(root, routing)
}

private class GroupShower(
    private val groupLoader: GroupLoader,
) {
    private val obv =
        observableOf<LoadGroupResp>(null) {
            setUpdater {
                groupLoader.load()
            }
        }

    fun show(
        root: Container,
        routing: Routing,
    ) {
        obv.sync(root) { resp ->
            if (resp == null) {
                return@sync
            }

            div {
                GroupHeaderShower.from(resp).load(this)
            }

            div(className = "row") {
                div(className = "col") {
                    GroupMemberShower.from(resp).show(this)
                }
                div(className = "col") {
                    GroupNoticeShower.from(resp).load(this)
                }
            }
        }
    }
}
