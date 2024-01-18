package cn.llonvne.site

import cn.llonvne.compoent.navigateButton
import cn.llonvne.constants.Frontend
import cn.llonvne.model.AuthenticationModel
import io.kvision.Application
import io.kvision.core.Container
import io.kvision.html.button
import io.kvision.html.div
import io.kvision.html.h1
import io.kvision.html.p
import io.kvision.panel.root
import io.kvision.routing.Routing
import io.kvision.state.bind

fun Container.index(routing: Routing) {
    repeat(10) {
        div(className = "") { +"Hello" }
    }
}
