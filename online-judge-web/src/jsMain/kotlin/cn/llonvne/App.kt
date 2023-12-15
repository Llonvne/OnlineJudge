package cn.llonvne

import io.kvision.*
import io.kvision.html.Div
import io.kvision.html.button
import io.kvision.html.label
import io.kvision.panel.root
import io.kvision.remote.getService
import kotlinx.browser.window
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.launch

val AppScope = CoroutineScope(window.asCoroutineDispatcher())

class App : Application() {
    override fun start() {
        val pingService = getService<IPingService>()

        val root = root("kvapp") {
        }

        AppScope.launch {
            root.add(Div {
                val label = label { }

                button("Click Me to Ping!!!") {
                    AppScope.launch {
                        label.content += pingService.ping("Hello")
                    }
                }
            })
        }
    }
}

fun main() {
    startApplication(::App, module.hot, BootstrapModule, BootstrapCssModule, CoreModule)
}
