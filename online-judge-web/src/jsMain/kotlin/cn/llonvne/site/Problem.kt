package cn.llonvne.site

import cn.llonvne.AppScope
import cn.llonvne.compoent.badgeGroup
import cn.llonvne.compoent.problemStatus
import cn.llonvne.dtos.ProblemListDto
import cn.llonvne.message.Messager
import cn.llonvne.model.ProblemModel
import cn.llonvne.model.Storage
import io.ktor.client.request.forms.*
import io.kvision.core.*
import io.kvision.form.formPanel
import io.kvision.form.text.Text
import io.kvision.html.*
import io.kvision.panel.*
import io.kvision.routing.Routing
import io.kvision.state.ObservableList
import io.kvision.state.ObservableListWrapper
import io.kvision.state.bind
import io.kvision.tabulator.ColumnDefinition
import io.kvision.tabulator.Layout
import io.kvision.tabulator.TabulatorOptions
import io.kvision.tabulator.tabulator
import io.kvision.utils.px
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.serializer

@Serializable
data class ProblemSearch(
    val input: String? = null
)

internal fun Container.problems(routing: Routing) {
    tabPanel(tabPosition = TabPosition.TOP) {
        tab("题目列表") {
            div {
                problemsList(routing) { title, component ->
                    addTab(title, component, closable = true)
                }
            }
        }
    }
}

private fun Container.problemsList(routing: Routing, tabAdder: ((String, Component) -> Unit)? = null) {
    val problems: ObservableList<ProblemListDto> = ObservableListWrapper()
    AppScope.launch {
        problems.addAll(
            ProblemModel.listProblem()
        )
    }

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
                    AppScope.launch {
                        problems.clear()
                        val result = ProblemModel.listProblem()
                        problems.addAll(result)
                        Messager.toastInfo("为你查找到 ${result.size}条记录")
                    }
                } else {
                    Messager.toastInfo("正在查找 ${data.input}")
                    disabled = true
                    AppScope.launch {
                        problems.clear()
                        val result = ProblemModel.search(data.input)

                        problems.addAll(result)
                        disabled = false
                        Messager.toastInfo("为你查找到 ${result.size}条记录")
                    }
                }
            }
        }
    }

    div().bind(problems) {
        tabulator(
            it, options = TabulatorOptions(
                layout = Layout.FITCOLUMNS, columns = listOf(
                    ColumnDefinition("题目ID", "problem.problemId"),
                    ColumnDefinition("题目名称", "problem.problemName", cellClick = { _, cell ->
                        val id = cell.getData.invoke().asDynamic().problem.problemId as Int?
                        val name = cell.getData.invoke().asDynamic().problem.problemName as String
                        if (tabAdder == null) {
                            routing.navigate("/problems/${id}")
                        } else {
                            tabAdder(name, Div {
                                detail(routing, id ?: -1)
                            })
                        }
                    }),
                    ColumnDefinition("状态", "status", formatterComponentFunction = { _, _, e: ProblemListDto ->
                        span {
                            problemStatus(e.status)
                        }
                    }),
                    ColumnDefinition("题目标签", formatterComponentFunction = { _, _, e: ProblemListDto ->
                        span {
                            badgeGroup(e.tags) { tag ->
                                +tag.tag
                            }
                        }
                    }),
                    ColumnDefinition("作者", formatterComponentFunction = { _, _, e: ProblemListDto ->
                        span {
                            +e.author.authorName
                        }
                    }),
                    ColumnDefinition("更新时间", formatterComponentFunction = { _, _, e: ProblemListDto ->
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

private fun Container.hello() {
    h1 {
        +"HelloWorld"
    }
}