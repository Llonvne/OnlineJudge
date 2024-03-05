package cn.llonvne.site.contest

import cn.llonvne.compoent.AlertType
import cn.llonvne.compoent.alert
import cn.llonvne.model.AuthenticationModel
import io.kvision.core.Container
import io.kvision.html.div
import io.kvision.html.h4
import io.kvision.state.bind

fun participantContestList(root: Container) {
    root.div().bind(AuthenticationModel.userToken) {
        if (it == null) {
            alert(AlertType.Info) {
                h4 {
                    +"你还未登入，无法获取已经参加的比赛"
                }
            }
        } else {

        }
    }
}