package cn.llonvne.site.contest

import cn.llonvne.compoent.AlertType
import cn.llonvne.compoent.alert
import cn.llonvne.model.RoutingModule
import io.kvision.core.Container
import io.kvision.core.onClickLaunch
import io.kvision.form.formPanel
import io.kvision.form.text.Text
import io.kvision.html.button
import io.kvision.html.h4
import io.kvision.html.p
import kotlinx.serialization.Serializable

@Serializable
private data class ContestIdForm(
    val id: String,
)

interface ContestNavigator {
    fun show(container: Container)

    companion object {
        fun get(): ContestNavigator =
            object : ContestNavigator {
                override fun show(container: Container) {
                    container.alert(AlertType.Secondary) {
                        h4 {
                            +"输入比赛 ID 或者 Hash 快速转到比赛"
                        }

                        p {
                            +"对于特定比赛可能只能通过 Hash 访问"
                        }

                        formPanel<ContestIdForm> {
                            add(ContestIdForm::id, Text(label = "比赛ID/Hash"), required = true)

                            button("转到") {
                                onClickLaunch {
                                    RoutingModule.routing.navigate("/contest/${getData().id}")
                                }
                            }
                        }
                    }
                }
            }
    }
}
