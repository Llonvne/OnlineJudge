package cn.llonvne

import cn.llonvne.constants.Frontend
import cn.llonvne.site.*
import io.kvision.*
import io.kvision.navigo.Match
import io.kvision.routing.Routing
import io.kvision.theme.ThemeManager
import kotlinx.browser.window
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.asCoroutineDispatcher
import kotlin.js.RegExp

val AppScope = CoroutineScope(window.asCoroutineDispatcher())

class App : Application() {
    init {
        ThemeManager.init()
    }

    override fun start() {
        val routing = Routing.init()

        routing.on(Frontend.Index.uri, {
            layout(routing) {
                index(routing)
            }
        }).on(Frontend.Problems.uri, {
            layout(routing) {
                problems(routing)
            }
        }).on(Frontend.Register.uri, {
            layout(routing) {
                registerPanel(routing)
            }
        }).on(Frontend.Login.uri, {
            layout(routing) {
                loginPanel(routing)
            }
        }).on(Frontend.Problems.Create.uri, {
            layout(routing) {
                createProblem(routing)
            }
        }).on(RegExp("^problems/(.*)"), { match: Match ->
            layout(routing) {
                detail(routing, (match.data[0] as String).toInt())
            }
        }).on(Frontend.Mine.uri, {
            layout(routing) {
                mine()
            }
        }).on(Frontend.Submission.uri, {
            layout(routing) {
                submission(routing)
            }
        }).on(RegExp("^code/(.*)"), { match: Match ->
            layout(routing) {
                code(routing, (match.data[0] as String).toInt())
            }
        })
            .resolve()
        Unit
    }
}


fun main() {
    startApplication(
        ::App,
        module.hot,
        BootstrapModule,
        BootstrapCssModule,
        CoreModule,
        TabulatorModule,
        ChartModule,
        TabulatorCssBootstrapModule
    )
}
