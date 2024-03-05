package cn.llonvne.site.contest

import cn.llonvne.compoent.AlertType
import cn.llonvne.compoent.alert
import io.kvision.core.Container
import io.kvision.html.div
import io.kvision.html.h1
import io.kvision.html.h4
import io.kvision.html.p

fun Container.contest() {
    alert(AlertType.Light) {
        h1 {
            +"比赛"
        }

        p {
            +"加入或者创建比赛，在限定的时间与别人一决高下吧"
        }
    }

    div(className = "row") {
        div(className = "col") {
            participantContestList(div { })
            createContestEntry(div { })
        }

        div(className = "col") {

        }
    }
}