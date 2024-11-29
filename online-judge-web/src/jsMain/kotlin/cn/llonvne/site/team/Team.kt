package cn.llonvne.site.team

import cn.llonvne.compoent.AlertType
import cn.llonvne.compoent.alert
import io.kvision.core.Container
import io.kvision.html.div
import io.kvision.html.h1
import io.kvision.html.p

interface TeamIndex {
    fun show(root: Container)

    companion object {
        fun from(): TeamIndex = TeamIndexBase()
    }
}

class TeamIndexBase : TeamIndex {
    override fun show(root: Container) {
        root.div {
            alert(AlertType.Light) {
                h1 {
                    +"队伍"
                }

                p {
                    +"众人拾柴火焰高"
                }
            }
        }
    }
}
