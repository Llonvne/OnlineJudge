package cn.llonvne

import cn.llonvne.compoent.AlertType
import cn.llonvne.compoent.alert
import cn.llonvne.constants.Frontend
import cn.llonvne.entity.group.GroupId
import cn.llonvne.model.RoutingModule
import cn.llonvne.site.*
import cn.llonvne.site.contest.ContestDetail
import cn.llonvne.site.contest.contest
import cn.llonvne.site.contest.createContest
import cn.llonvne.site.mine.mine
import cn.llonvne.site.problem.createProblem
import cn.llonvne.site.problem.detail.detail
import cn.llonvne.site.problem.problems
import cn.llonvne.site.share.CodeLoader
import cn.llonvne.site.share.share
import cn.llonvne.site.team.TeamIndex
import cn.llonvne.site.team.show.showGroup
import cn.llonvne.site.team.teamCreate
import io.kvision.*
import io.kvision.html.div
import io.kvision.navigo.Match
import io.kvision.navigo.Navigo
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

    private fun failTo404(routing: Routing, block: () -> Unit) = kotlin.runCatching { block() }.onFailure {
        routing.navigate("/404")
        throw it
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
                    detail(div { }, (match.data[0] as String).toInt())
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
        }).on("/contest/create", {
            failTo404(routing) {
                layout(routing) {
                    createContest()
                }
            }
        })
            .on("/contest", {
                failTo404(routing) {
                    layout(routing) {
                        contest()
                    }
                }
            })
            .on(RegExp("^contest/(.*)"), {
                failTo404(routing) {
                    layout(routing) {
                        ContestDetail.from(it.data[0] as String).show(div { })
                    }
                }
            })
            .on(RegExp("^share/(.*)"), {
                failTo404(routing) {
                    layout(routing) {
                        val id = it.data[0] as String

                        val intId = id.toIntOrNull()

                        if (intId != null) {
                            share(
                                intId, CodeLoader.id()
                            )
                        } else {
                            share(
                                id, CodeLoader.hash()
                            )
                        }
                    }
                }
            }).injectTeam(routing).on("/404", {
                layout(routing) {
                    alert(AlertType.Danger) {
                        +"解析失败了"
                    }
                }
            }).notFound(
                {
                    routing.navigate("/404")
                },
            ).resolve()
        Unit
    }

    private fun Navigo.injectTeam(routing: Routing): Navigo {
        return on("/team", {
            failTo404(routing) {
                layout(routing) {
                    TeamIndex.from().show(div { })
                }
            }
        }).on("/team/create", {
            failTo404(routing) {
                layout(routing) {
                    teamCreate(this, routing)
                }
            }
        }).on(RegExp("^team/(.*)"), { match: Match ->
            failTo404(routing) {
                layout(routing) {
                    val id = match.data[0] as String
                    val intId = id.toIntOrNull()
                    if (intId != null) {
                        showGroup(div { }, GroupId.IntGroupId(intId), routing)
                    } else if (id.length == 36) {
                        showGroup(div { }, GroupId.HashGroupId(id), routing)
                    } else {
                        showGroup(div { }, GroupId.ShortGroupName(id), routing)
                    }
                }
            }
        })
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
