package cn.llonvne.site

import cn.llonvne.compoent.observable.observableOf
import cn.llonvne.entity.group.GroupId
import cn.llonvne.entity.group.GroupLoader
import cn.llonvne.kvision.service.IGroupService.LoadGroupResp
import cn.llonvne.model.TeamModel.of
import io.kvision.core.Container
import io.kvision.html.div
import io.kvision.routing.Routing

fun showTeam(root: Container, groupId: GroupId, routing: Routing) {
    val teamLoader = GroupLoader.Companion.of(groupId)
    root.div {
        observableOf<LoadGroupResp>(null) {
            setUpdater {
                teamLoader.load(groupId)
            }
        }
    }
}