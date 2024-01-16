package cn.llonvne

import cn.llonvne.panel.loginPanel
import cn.llonvne.panel.registerPanel
import io.kvision.*
import io.kvision.html.button
import io.kvision.html.h1
import io.kvision.panel.root
import io.kvision.routing.Routing
import io.kvision.routing.Strategy
import kotlinx.browser.window
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.launch

val AppScope = CoroutineScope(window.asCoroutineDispatcher())

class App : Application() {
    override fun start() {
        val routing = Routing.init(strategy = Strategy.ALL)

        routing
            .on("/", {
                root("kvapp") {
                    h1 {
                        +"Online Judge"
                    }
                    button("a") {
                        onClick {
                            AppScope.launch {
                                routing.navigate("/a")
                            }
                        }
                    }
                }
            })
            .on("/register", {
                root("kvapp") {
                    registerPanel()
                }
            })
            .on("/login", {
                root("kvapp") {
                    loginPanel(routing)
                }
            })
            .resolve()
    }
}

fun main() {
    startApplication(::App, module.hot, BootstrapModule, BootstrapCssModule, CoreModule)
}
