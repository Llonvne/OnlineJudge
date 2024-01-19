package cn.llonvne

import cn.llonvne.compoent.layout.footer
import cn.llonvne.compoent.layout.header
import io.kvision.Application
import io.kvision.core.Container
import io.kvision.html.div
import io.kvision.panel.root
import io.kvision.routing.Routing

fun Application.layout(routing: Routing, build: Container.() -> Unit) {
    root("kvapp") {
        header(routing)
        div(className = "px-4") {
            build()
        }
        footer()
    }
}