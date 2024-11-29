package cn.llonvne.compoent

import io.kvision.core.Container
import io.kvision.html.Div
import io.kvision.html.div
import io.kvision.html.h1
import io.kvision.html.p

enum class AlertType(
    val id: String,
) {
    Primary("primary"),
    Secondary("secondary"),
    Success("success"),
    Danger("danger"),
    Warning("warning"),
    Info("info"),
    Light("light"),
    Dark("dark"),
}

fun Container.alert(
    type: AlertType,
    init: Div.() -> Unit,
) {
    div(className = "p-2") {
        div(className = "alert alert-${type.id}") {
            setAttribute("role", "alert")
            init()
        }
    }
}

fun Container.loading() {
    alert(AlertType.Light) {
        h1 { +"正在加载中" }
        p {
            +"请耐心等待哦..."
        }
    }
}
