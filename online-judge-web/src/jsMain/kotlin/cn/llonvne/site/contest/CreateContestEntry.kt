package cn.llonvne.site.contest

import cn.llonvne.compoent.AlertType
import cn.llonvne.compoent.alert
import cn.llonvne.model.RoutingModule
import io.kvision.core.Container
import io.kvision.html.ButtonStyle
import io.kvision.html.button
import io.kvision.html.h4
import io.kvision.html.p

fun createContestEntry(root: Container) {
    root.alert(AlertType.Success) {
        h4 {
            +"没有想参加的比赛？，自己来组织吧！"
        }

        p {
            +"点击下方链接即可创建比赛"
        }

        button("创建比赛", style = ButtonStyle.OUTLINESECONDARY) {
            onClick {
                RoutingModule.routing.navigate("/contest/create")
            }
        }
    }
}
