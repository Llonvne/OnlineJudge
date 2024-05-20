package cn.llonvne.site.problem

import cn.llonvne.compoent.AlertType
import cn.llonvne.compoent.alert
import cn.llonvne.compoent.badgeGroup
import cn.llonvne.compoent.observable.observableListOf
import cn.llonvne.compoent.observable.observableOf
import cn.llonvne.compoent.problemStatus
import cn.llonvne.dtos.ProblemForList
import cn.llonvne.entity.problem.ProblemListShowType
import cn.llonvne.message.Messager
import cn.llonvne.model.ProblemModel
import cn.llonvne.model.RoutingModule
import cn.llonvne.model.Storage
import io.kvision.core.Container
import io.kvision.core.onClickLaunch
import io.kvision.core.onInput
import io.kvision.form.formPanel
import io.kvision.form.text.Text
import io.kvision.html.*
import io.kvision.routing.Routing
import io.kvision.state.bind
import io.kvision.tabulator.ColumnDefinition
import io.kvision.tabulator.Layout
import io.kvision.tabulator.TabulatorOptions
import io.kvision.tabulator.tabulator
import io.kvision.toolbar.buttonGroup
import io.kvision.utils.px
import kotlinx.serialization.Serializable
import kotlinx.serialization.serializer

@Serializable
data class ProblemSearch(
    val input: String? = null
)

internal fun Container.problems(routing: Routing) {

    alert(AlertType.Light) {
        h1 {
            +"题目集合"
        }

        p {
            +"选择或者搜索您喜欢的题目来练练手吧！"
        }
    }

    observableOf(ProblemListConfigure()) {

        setUpdater { ProblemListConfigure() }

        syncNotNull(div { }) { config ->
            div(className = "row") {
                div(className = "col-8") {
                    alert(AlertType.Light) {
                        problemsList(routing, config)
                    }
                }
                div(className = "col") {
                    alert(AlertType.Light) {
                        buttonGroup {
                            button("所有") {
                                onClick {
                                    setObv(ProblemListConfigure {
                                        showType = ProblemListShowType.All
                                    })
                                }
                            }
                            button("已通过", style = ButtonStyle.SUCCESS) {
                                onClick {
                                    setObv(ProblemListConfigure {
                                        showType = ProblemListShowType.Accepted
                                    })
                                }
                            }
                            button("已尝试", style = ButtonStyle.WARNING) {
                                onClick {
                                    setObv(ProblemListConfigure {
                                        showType = ProblemListShowType.Attempted
                                    })
                                }
                            }
                            button("星标", style = ButtonStyle.INFO) {
                                onClick {
                                    setObv(ProblemListConfigure {
                                        showType = ProblemListShowType.Favorite
                                    })
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}



private class ProblemListConfigure {
    var showType: ProblemListShowType = ProblemListShowType.All
}

private fun ProblemListConfigure(configure: ProblemListConfigure.() -> Unit): ProblemListConfigure {
    val config = ProblemListConfigure()
    config.configure()
    return config
}

private fun Container.problemsList(
    routing: Routing,
    configure: ProblemListConfigure = ProblemListConfigure()
) {

    val loader: suspend () -> List<ProblemForList> = {
        ProblemModel.listProblem(configure.showType)
    }

    val alert = div { }

    add(alert)

    observableListOf {
        setUpdater { ProblemModel.listProblem() }
        formPanel<ProblemSearch> {
            "row gy-2 gx-3 align-items-center p-1".split(" ").forEach {
                addCssClass(it)
            }

            var searchText by Storage.remember("", "problemSearchText")

            add(ProblemSearch::input, Text(value = searchText) {
                placeholder = "查找"
                addCssClass("col-auto")
                removeCssClass("kv-mb-3")

                onInput {
                    searchText = this.value ?: ""
                }

            }, required = true, requiredMessage = "查找内容不可为空")
            button("搜索", style = ButtonStyle.OUTLINESECONDARY) {
                addCssClass("col-auto")
                onClick {
                    val data = this@formPanel.getData()

                    if (data.input == null) {
                        updateList {
                            ProblemModel.listProblem().also {
                                Messager.toastInfo("为你查找到 ${it.size}条记录")
                            }
                        }
                    } else {
                        Messager.toastInfo("正在查找 ${data.input}")
                        disabled = true

                        updateList {
                            ProblemModel.search(data.input).also {
                                disabled = false
                                Messager.toastInfo("为你查找到 ${it.size}条记录")
                            }
                        }
                    }
                }
            }
            button("创建您的题目", style = ButtonStyle.OUTLINESECONDARY) {
                addCssClass("col-auto")
                onClickLaunch {
                    RoutingModule.routing.navigate("/problems/create")
                }
            }
        }

        div(className = "py-2").bind(this) {
            tabulator(
                it, options = TabulatorOptions(
                    layout = Layout.FITCOLUMNS, columns = listOf(
                        ColumnDefinition("题目ID", "problem.problemId"),
                        ColumnDefinition("题目名称", "problem.problemName", cellClick = { _, cell ->
                            val id = cell.getData.invoke().asDynamic().problem.problemId as Int?
                            routing.navigate("/problems/${id}")
                        }),
                        ColumnDefinition("状态", "status", formatterComponentFunction = { _, _, e: ProblemForList ->
                            span {
                                problemStatus(e.status)
                            }
                        }),
                        ColumnDefinition("题目标签", formatterComponentFunction = { _, _, e: ProblemForList ->
                            span {
                                badgeGroup(e.tags) { tag ->
                                    +tag.tag
                                }
                            }
                        }),
                        ColumnDefinition("作者", formatterComponentFunction = { _, _, e: ProblemForList ->
                            span {
                                +e.author.authorName
                            }
                        }),
                        ColumnDefinition("更新时间", formatterComponentFunction = { _, _, e: ProblemForList ->
                            span {
                                val updateAt = e.problem.updatedAt
                                if (updateAt != null) {
                                    +"${updateAt.year}年${updateAt.monthNumber}月${updateAt.dayOfMonth}日"
                                } else {
                                    +"未找到更新时间"
                                }
                            }
                        })
                    )
                ), serializer = serializer()
            ) {
                height = 400.px
            }
        }
    }


}