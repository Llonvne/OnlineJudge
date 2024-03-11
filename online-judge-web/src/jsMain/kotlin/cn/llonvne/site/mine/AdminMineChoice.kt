package cn.llonvne.site.mine

import cn.llonvne.compoent.AlertType
import cn.llonvne.compoent.alert
import io.kvision.core.Container
import io.kvision.html.div
import io.kvision.html.p


sealed interface AdminMineChoice {
    val name: String

    fun show(root: Container)

    companion object {

        private val choices by lazy {
            listOf(
                DashBoard(),
                UserManage(),
                SystemSettings(),
                ProblemManage(),
                ContestManage()
            )
        }

        internal fun getAdminMineChoices() = choices

        internal fun defaultChoice() = choices.first()
    }
}





data class SystemSettings(override val name: String = "系统设置") : AdminMineChoice {
    override fun show(root: Container) {
        root.div {
            p {
                +name
            }
        }
    }
}

data class ProblemManage(override val name: String = "题目管理") : AdminMineChoice {
    override fun show(root: Container) {
        root.div {
            p {
                +name
            }
        }
    }
}

data class ContestManage(override val name: String = "比赛管理") : AdminMineChoice {
    override fun show(root: Container) {
        root.div {
            p {
                +name
            }
        }
    }
}


