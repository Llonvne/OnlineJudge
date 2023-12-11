package cn.llonvne

import io.kvision.*
import io.kvision.html.button
import io.kvision.html.h1
import io.kvision.panel.root
import io.kvision.remote.getService
import kotlinx.browser.window
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.launch

val AppScope = CoroutineScope(window.asCoroutineDispatcher())

class App : Application() {

    private val pingService = getService<IPingService>()

    override fun start() {
        root("kvapp") {
            val h1 = h1 {
                +"HelloWorld"
            }

            button("ClickMe") {
                onClick {
                    AppScope.launch {
                        pingService.ping("Hello")
                    }
                }
            }

            AppScope.launch {
                pingService.ping("Hello").let {
                    println(it)
                    h1.content = it
                }
            }
        }

    }
}

fun main() {
    startApplication(::App, module.hot, BootstrapModule, BootstrapCssModule, CoreModule)
}
