package cn.llonvne.site

import cn.llonvne.compoent.AlertType
import cn.llonvne.compoent.alert
import cn.llonvne.compoent.observable.observableOf
import io.kvision.core.Container
import io.kvision.core.onChangeLaunch
import io.kvision.core.onInput
import io.kvision.form.formPanel
import io.kvision.form.text.Text
import io.kvision.html.div
import io.kvision.html.h4
import io.kvision.html.label
import io.kvision.routing.Routing
import io.kvision.state.bind
import kotlinx.serialization.Serializable

@Serializable
private data class CreateTeam(
    val teamName: String,
    val shortName: String
)

fun teamCreate(root: Container, routing: Routing) {
    root.div {

        div(className = "row") {

            alert(AlertType.Primary) {
                h4 {
                    +"创建队伍"
                }
            }

            div(className = "col-6") {
                formPanel<CreateTeam> {
                    add(CreateTeam::teamName, Text {
                        label = "你的组织的名字"
                    })

                    observableOf("") {
                        val text = Text().bind(this) {
                            label = "短名称"
                            onChangeLaunch {
                                setObv(this.value ?: "<短名称>")
                            }
                        }
                        label().bind(this) {
                            +"你可以通过 /t/${it} 访问您的组织"
                        }
                        add(CreateTeam::teamName, text)
                    }
                }
            }

            div(className = "col-6") {

            }
        }


    }
}