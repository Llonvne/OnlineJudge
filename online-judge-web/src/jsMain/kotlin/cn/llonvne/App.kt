package cn.llonvne

import io.kvision.Application
import io.kvision.BootstrapCssModule
import io.kvision.BootstrapModule
import io.kvision.CoreModule
import io.kvision.core.AlignItems
import io.kvision.core.JustifyContent
import io.kvision.form.text.textInput
import io.kvision.html.*
import io.kvision.module
import io.kvision.panel.hPanel
import io.kvision.panel.root
import io.kvision.panel.vPanel
import io.kvision.remote.getService
import io.kvision.startApplication
import io.kvision.utils.perc
import io.kvision.utils.px
import kotlinx.browser.window
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.launch

val AppScope = CoroutineScope(window.asCoroutineDispatcher())

class App : Application() {

    val pingService = getService<IPingService>()

    override fun start() {
        root("kvapp") {
            val h1 = h1 {
            }

            AppScope.launch {
                pingService.ping("Hello").let {
                    h1.content = it
                }
            }
        }

    }
}

fun main() {
    startApplication(::App, module.hot, BootstrapModule, BootstrapCssModule, CoreModule)
}
