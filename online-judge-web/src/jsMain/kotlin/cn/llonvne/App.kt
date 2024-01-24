package cn.llonvne

import cn.llonvne.compoent.AlertType
import cn.llonvne.compoent.alert
import cn.llonvne.constants.Frontend
import cn.llonvne.model.RoutingModule
import cn.llonvne.site.*
import cn.llonvne.site.share.CodeLoader
import cn.llonvne.site.share.ShareCodeHighlighter
import cn.llonvne.site.share.share
import io.kvision.*
import io.kvision.html.div
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

    fun failTo404(routing: Routing, block: () -> Unit) = kotlin.runCatching { block() }.onFailure {
        routing.navigate("/404")
    }

    override fun start() {
        val routing = RoutingModule.routing

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
        }).on(Frontend.Problems.Create.uri, {
            layout(routing) {
                createProblem(routing)
            }
        }).on(RegExp("^problems/(.*)"), { match: Match ->
            failTo404(routing) {
                layout(routing) {
                    detail(routing, (match.data[0] as String).toInt())
                }
            }
        }).on(Frontend.Mine.uri, {
            layout(routing) {
                mine()
            }
        }).on(Frontend.Submission.uri, {
            layout(routing) {
                submission(routing)
            }
        }).on("/playground", { match: Match ->
            layout(routing) {
                playground()
            }
        }).on(RegExp("^code/(.*)"), { match: Match ->
            failTo404(routing) {
                layout(routing) {
                    submissionDetail(routing, (match.data[0] as String).toInt())
                }
            }
        }).on(RegExp("^share/(.*)"), {
            failTo404(routing) {
                layout(routing) {
                    val id = it.data[0] as String

                    val intId = id.toIntOrNull()
                    val alert = div { }

                    if (intId != null) {
                        share(
                            intId,
                            CodeLoader.id(),
                            alert
                        )
                    } else {
                        share(
                            id, CodeLoader.hash(),
                            alert
                        )
                    }
                }
            }
        }).on("/404", {
            layout(routing) {
                alert(AlertType.Danger) {
                    +"解析失败了"
                }
            }
        }).resolve()
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
        TabulatorCssBootstrapModule,
        ChartModule,
        FontAwesomeModule,
        ToastifyModule,
        TomSelectModule,
    )
}
