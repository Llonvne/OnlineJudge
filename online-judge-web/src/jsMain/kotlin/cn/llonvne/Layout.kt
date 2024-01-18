package cn.llonvne

import cn.llonvne.compoent.layout.footer
import cn.llonvne.compoent.layout.header
import cn.llonvne.constants.Frontend
import cn.llonvne.model.AuthenticationModel
import io.kvision.Application
import io.kvision.core.*
import io.kvision.dropdown.dropDown
import io.kvision.form.check.checkBox
import io.kvision.form.text.text
import io.kvision.html.div
import io.kvision.html.p
import io.kvision.navbar.*
import io.kvision.panel.root
import io.kvision.panel.stackPanel
import io.kvision.routing.Routing
import io.kvision.state.bind
import io.kvision.theme.Theme
import io.kvision.theme.ThemeManager

fun Application.layout(routing: Routing, build: Container.() -> Unit) {
    root("kvapp") {
        header(routing)
        div(className = "px-4") {
            build()
        }
        footer()
    }
}