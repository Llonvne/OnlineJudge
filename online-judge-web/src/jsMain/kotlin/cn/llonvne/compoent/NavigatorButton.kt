package cn.llonvne.compoent

import cn.llonvne.constants.Site
import io.kvision.core.Container
import io.kvision.html.button
import io.kvision.routing.Routing

fun Container.navigateButton(routing: Routing, to: Site) {
    button(to.name) {
        onClick {
            routing.navigate(to.uri)
        }
    }
}