package cn.llonvne.site

import cn.llonvne.compoent.badgeGroup
import cn.llonvne.compoent.observable.observableListOf
import cn.llonvne.compoent.problemStatus
import cn.llonvne.dtos.ProblemListDto
import cn.llonvne.message.Messager
import cn.llonvne.model.ProblemModel
import cn.llonvne.model.Storage
import io.kvision.core.Container
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
import io.kvision.utils.px
import kotlinx.serialization.Serializable
import kotlinx.serialization.serializer

@Serializable
data class ProblemSearch(
    val input: String? = null
)

internal fun Container.problems(routing: Routing) {
    div(className = "row") {
        div(className = "col-8") {
            problemsList(routing)
        }
        div(className = "col") {
            h1 { +"HelloWorld" }
        }
    }
}

private fun Container.problemsList(routing: Routing) {

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


}